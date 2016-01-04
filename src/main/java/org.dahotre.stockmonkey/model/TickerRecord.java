package org.dahotre.stockmonkey.model;

import com.amazonaws.util.json.JSONObject;

/**
 * Represents the Ticker record
 */
public class TickerRecord {
  public TickerRecord(String symbol) {
    this.symbol = symbol;
  }

  public TickerRecord() {
  }

  private String symbol;

  public String getSymbol() {
    return symbol;
  }

  public void setSymbol(String symbol) {
    this.symbol = symbol;
  }

  @Override
  public String toString() {
    return new JSONObject(this).toString();
  }
}
