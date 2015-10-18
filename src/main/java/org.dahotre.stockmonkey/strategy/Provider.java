package org.dahotre.stockmonkey.strategy;

import org.dahotre.stockmonkey.exception.RetrievalException;
import org.dahotre.stockmonkey.model.TickerRecord;

import java.util.List;

/**
 * Represents the provider of ticker data
 */
public interface Provider {

  /**
   * Retrieves the Tickers from relevant sources
   * @param market
   * @return
   */
  List<TickerRecord> retrieve(String market) throws RetrievalException;

  double getPrice(TickerRecord tickerRecord);
}
