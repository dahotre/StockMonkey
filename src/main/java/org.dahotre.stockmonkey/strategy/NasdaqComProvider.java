package org.dahotre.stockmonkey.strategy;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.dahotre.stockmonkey.exception.RetrievalException;
import org.dahotre.stockmonkey.model.NasdaqComTickerRecord;
import org.dahotre.stockmonkey.model.TickerRecord;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Retrieves Ticker symbols from Nasdaq.com
 */
public class NasdaqComProvider implements Provider {
  private static final String nasdaqComUrlPattern = "http://www.nasdaq.com/screening/companies-by-industry.aspx?exchange=%s&render=download";
  public List<TickerRecord> retrieve(String market) throws RetrievalException {
    System.out.println("Retrieving all tickers from Nasdaq.com for: " + market);
    try {
      URL nasdaqComUrl = new URL(String.format(nasdaqComUrlPattern, market));
      final CSVParser csvRecords = CSVParser.parse(nasdaqComUrl, Charset.defaultCharset(), CSVFormat.DEFAULT);
      return csvRecords.getRecords().stream()
          .filter(csvRecord -> {
            try {
              Double.parseDouble(csvRecord.get(2));
            }
            catch (NumberFormatException nfe) {
              return false;
            }
            return true;
            })
            .map(csvRecord -> {
              NasdaqComTickerRecord nasdaqComTickerRecord = new NasdaqComTickerRecord();
              nasdaqComTickerRecord.setSymbol(csvRecord.get(0));
              nasdaqComTickerRecord.setPrice(Double.parseDouble(csvRecord.get(2)));
              return nasdaqComTickerRecord;
            })
                .collect(Collectors.<TickerRecord>toList());
          }
      catch (IOException e) {
      throw new RetrievalException(e);
    }
  }

  @Override
  public double getPrice(TickerRecord tickerRecord) {
    if (!(tickerRecord instanceof NasdaqComTickerRecord)) {
      throw new RetrievalException("I can only understand NasdaqComTickerRecord");
    }
    return ((NasdaqComTickerRecord) tickerRecord).getPrice();
  }
}
