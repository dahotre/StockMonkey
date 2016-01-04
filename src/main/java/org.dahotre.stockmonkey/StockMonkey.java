package org.dahotre.stockmonkey;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBAsyncClient;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.ListDomainsResult;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.sns.AmazonSNSClient;
import org.dahotre.stockmonkey.model.LambdaEvent;
import org.dahotre.stockmonkey.model.MarketName;
import org.dahotre.stockmonkey.model.TickerRecord;
import org.dahotre.stockmonkey.model.TransactionSnapshot;
import org.dahotre.stockmonkey.strategy.MarketTickerGetter;
import org.dahotre.stockmonkey.strategy.NasdaqComProvider;
import org.dahotre.stockmonkey.strategy.Provider;
import org.dahotre.stockmonkey.strategy.YahooFinanceProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Retrieves all tickers and picks a stock at random. The only conditions applied are:
 * price between 1$ and 200$.
 */
public class StockMonkey {

  public String handleRequest(LambdaEvent lambdaEvent, Context context) {
    StringBuilder choiceBuilder = new StringBuilder();
    Provider tickerProvider = new NasdaqComProvider();
    List<TickerRecord> tickerRecords = new ArrayList<>();
    MarketTickerGetter marketTickerGetter = new MarketTickerGetter();
    for (MarketName marketName : MarketName.values()) {
      List<TickerRecord> marketSpecificTickers = marketTickerGetter.retrieveTickers(tickerProvider, marketName.name());

      if (marketSpecificTickers != null && !marketSpecificTickers.isEmpty()) {
        tickerRecords.addAll(marketSpecificTickers);
      }
    }

    int totalTickers = tickerRecords.size();
    Random random = new Random(System.currentTimeMillis());
    boolean isValidChosen = false;
    while (!isValidChosen) {
      int chosenIndex = random.nextInt(totalTickers);
      TickerRecord tickerRecord = tickerRecords.get(chosenIndex);
      double price = tickerProvider.getPrice(tickerRecord);
      if (1d > price || 200d < price) {
        context.getLogger().log(String.format("Rejecting %s at %f", tickerRecord.getSymbol(), price));
        isValidChosen = false;
      }
      else {
        choiceBuilder.append("ticker = " + tickerRecord.getSymbol());
        choiceBuilder.append("; price = " + price);
        choiceBuilder.append("; shares = " + (int) (200 / price));
        isValidChosen = true;

        AmazonSimpleDB db = new AmazonSimpleDBAsyncClient();
        db.setRegion(Region.getRegion(Regions.US_EAST_1));
        final ListDomainsResult listDomainsResult = db.listDomains();

        //Make sure that the domain exists. This will be useful only for the first run
        if (listDomainsResult.getDomainNames() == null || listDomainsResult.getDomainNames().isEmpty()) {
          db.createDomain(new CreateDomainRequest(TransactionSnapshot.DOMAIN_NAME));
        }

        final TransactionSnapshot transactionSnapshot = new TransactionSnapshot(
            tickerRecord.getSymbol()
            , price
            , (int) (200 / price)
            , new YahooFinanceProvider().getPrice(new TickerRecord(YahooFinanceProvider.SP500))
        );

        db.putAttributes(new PutAttributesRequest(
            TransactionSnapshot.DOMAIN_NAME
            , Long.toString(transactionSnapshot.getSnapshotId())
            , transactionSnapshot.toAttributes()
        ));
      }
    }

    AmazonSNSClient snsClient = new AmazonSNSClient();
    snsClient.setRegion(Region.getRegion(Regions.US_EAST_1));
    snsClient.publish("arn:aws:sns:us-east-1:117395751670:stock-monkey", choiceBuilder.toString());
    return choiceBuilder.toString();
  }
}
