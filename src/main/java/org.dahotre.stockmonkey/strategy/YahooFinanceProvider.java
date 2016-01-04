package org.dahotre.stockmonkey.strategy;

import com.amazonaws.util.IOUtils;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import org.dahotre.stockmonkey.exception.RetrievalException;
import org.dahotre.stockmonkey.model.TickerRecord;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

/**
 * Retrieves data from Yahoo Finance using YQL
 */
public class YahooFinanceProvider implements Provider {
  private static final String YQL_FORMAT_PART_1 = "https://query.yahooapis.com/v1/public/yql?q=select%20LastTradePriceOnly%20from%20yahoo.finance.quote%20where%20symbol%20in%20(%22";
  private static final String YQL_FORMAT_PART_2 = "%22)&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=";
  public static final String SP500 = "^GSPC";

  @Override
  public List<TickerRecord> retrieve(String market) throws RetrievalException {
    throw new UnsupportedOperationException("Use other providers for this operation");
  }

  @Override
  public double getPrice(TickerRecord tickerRecord) {
    try {
      URL yqlURL = new URL(YQL_FORMAT_PART_1 + URLEncoder.encode(tickerRecord.getSymbol(), "UTF-8") + YQL_FORMAT_PART_2);
      HttpURLConnection connection = (HttpURLConnection) yqlURL.openConnection();
      connection.setDoOutput(true);
      final JSONObject jsonObject = new JSONObject(IOUtils.toString(connection.getInputStream()));
      final String lastPrice = jsonObject.getJSONObject("query").getJSONObject("results").getJSONObject("quote").getString("LastTradePriceOnly");
      return Double.valueOf(lastPrice);
    } catch (IOException | JSONException e) {
      e.printStackTrace();
    }
    return 0;
  }
}
