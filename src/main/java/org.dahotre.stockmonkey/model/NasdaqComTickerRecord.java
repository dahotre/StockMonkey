package org.dahotre.stockmonkey.model;

/**
 * Ticker record, as returned by Nasdaq.com
 */
public class NasdaqComTickerRecord extends TickerRecord {
  double price;

  public double getPrice() {
    return price;
  }

  public void setPrice(double price) {
    this.price = price;
  }
}
