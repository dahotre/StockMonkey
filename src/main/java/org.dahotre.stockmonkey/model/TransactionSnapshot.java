package org.dahotre.stockmonkey.model;

import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;

import java.util.ArrayList;
import java.util.List;

/**
 * A snapshot of the transaction and SP500 price for reference.
 */
public class TransactionSnapshot {
  public static final String DOMAIN_NAME = "transactions";
  public static final String TICKER = "ticker";
  public static final String STOCK_PRICE = "stockPrice";
  public static final String NUM_OF_STOCKS = "numOfStocks";
  public static final String SP_500_PRICE = "sp500Price";

  long snapshotId;
  String ticker;
  double stockPrice;
  int numOfStocks;
  double sp500Price;

  public TransactionSnapshot(String ticker, double stockPrice, int numOfStocks, double sp500Price) {
    this.snapshotId = System.currentTimeMillis();
    this.ticker = ticker;
    this.stockPrice = stockPrice;
    this.numOfStocks = numOfStocks;
    this.sp500Price = sp500Price;
  }

  public TransactionSnapshot (Item item) {
    this.snapshotId = Long.valueOf(item.getName());
    for (Attribute attr : item.getAttributes()) {
      if (attr.getName().equals(TICKER)) {
        this.ticker = attr.getValue();
      }
      else if (attr.getName().equals(STOCK_PRICE)) {
        this.stockPrice = Double.parseDouble(attr.getValue());
      }
      else if (attr.getName().equals(NUM_OF_STOCKS)) {
        this.numOfStocks = Integer.parseInt(attr.getValue());
      }
      else if (attr.getName().equals(SP_500_PRICE)) {
        this.sp500Price = Double.parseDouble(attr.getValue());
      }
    }
  }

  public long getSnapshotId() {
    return snapshotId;
  }

  public void setSnapshotId(long snapshotId) {
    this.snapshotId = snapshotId;
  }

  public String getTicker() {
    return ticker;
  }

  public void setTicker(String ticker) {
    this.ticker = ticker;
  }

  public double getStockPrice() {
    return stockPrice;
  }

  public void setStockPrice(double stockPrice) {
    this.stockPrice = stockPrice;
  }

  public int getNumOfStocks() {
    return numOfStocks;
  }

  public void setNumOfStocks(int numOfStocks) {
    this.numOfStocks = numOfStocks;
  }

  public double getSp500Price() {
    return sp500Price;
  }

  public void setSp500Price(double sp500Price) {
    this.sp500Price = sp500Price;
  }

  public List<ReplaceableAttribute> toAttributes() {
    final List<ReplaceableAttribute> attributes = new ArrayList<>();
    attributes.add(new ReplaceableAttribute(TICKER, ticker, false));
    attributes.add(new ReplaceableAttribute(STOCK_PRICE, Double.toString(stockPrice), false));
    attributes.add(new ReplaceableAttribute(NUM_OF_STOCKS, Integer.toString(numOfStocks), false));
    attributes.add(new ReplaceableAttribute(SP_500_PRICE, Double.toString(sp500Price), false));
    return attributes;
  }

}
