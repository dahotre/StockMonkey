package org.dahotre.stockmonkey;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBAsyncClient;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.amazonaws.services.simpledb.model.SelectResult;
import com.amazonaws.services.sns.AmazonSNSClient;
import org.dahotre.stockmonkey.model.LambdaEvent;
import org.dahotre.stockmonkey.model.TickerRecord;
import org.dahotre.stockmonkey.model.TransactionSnapshot;
import org.dahotre.stockmonkey.strategy.Provider;
import org.dahotre.stockmonkey.strategy.YahooFinanceProvider;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Computes the difference in performance of Stock Monkey vs S&P 500.
 */
public class MonkeyVsWorld {

  /**
   * the method triggered by Lambda
   * @param lambdaEvent
   * @param context
   * @return
   */
  public String handleRequest(LambdaEvent lambdaEvent, Context context) {

    Provider provider = new YahooFinanceProvider();

    AmazonSimpleDB db = new AmazonSimpleDBAsyncClient();
    db.setRegion(Region.getRegion(Regions.US_EAST_1));

    SelectResult selectResult = db.select(new SelectRequest("select * from transactions"));
    List<TransactionSnapshot> transactionSnapshots = selectResult.getItems().stream()
        .map(item -> new TransactionSnapshot(item))
        .collect(Collectors.toList());

    //fractional SP500 stocks bought = total money spent / sp500 price
    double totalFractionalSP500Shares = transactionSnapshots.stream()
        .mapToDouble(snapshot ->
            (snapshot.getNumOfStocks() * snapshot.getStockPrice()) / snapshot.getSp500Price()
        )
        .sum();

    TickerRecord sp500 = new TickerRecord(YahooFinanceProvider.SP500);
    double sp500Value = totalFractionalSP500Shares * provider.getPrice(sp500);

    double portfolioValue = transactionSnapshots.stream()
        .mapToDouble(snapshot -> {
          String ticker = snapshot.getTicker();
          int numOfStocks = snapshot.getNumOfStocks();
          double currentPrice = provider.getPrice(new TickerRecord(ticker));
          return currentPrice * numOfStocks;
        })
        .sum();

    double costBasis = transactionSnapshots.stream()
        .mapToDouble(snapshot -> {
          int numOfStocks = snapshot.getNumOfStocks();
          double stockPrice = snapshot.getStockPrice();
          return numOfStocks * stockPrice;
        })
        .sum();

    double differencePercent = (portfolioValue - sp500Value) * 100 / sp500Value;
    String portfolioStatus = String.format("Your portfolio: $%.2f V/S S&P 500: $%.2f. I.e., %.2f%%. Cost basis: $%.2f", portfolioValue, sp500Value, differencePercent, costBasis);

    AmazonSNSClient snsClient = new AmazonSNSClient();
    snsClient.setRegion(Region.getRegion(Regions.US_EAST_1));
    snsClient.publish("arn:aws:sns:us-east-1:117395751670:stock-monkey", portfolioStatus);
    return portfolioStatus;
  }
}
