package org.dahotre.stockmonkey.strategy;

import org.dahotre.stockmonkey.model.TickerRecord;

import java.util.List;

/**
 * Downloads the Tickers for a given Market
 */
public class MarketTickerGetter {
  public List<TickerRecord> retrieveTickers(Provider provider, String market) {
    return provider.retrieve(market);
  }
}
