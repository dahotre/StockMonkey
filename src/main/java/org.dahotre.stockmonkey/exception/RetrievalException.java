package org.dahotre.stockmonkey.exception;

/**
 * To be thrown during any retrieval process
 */
public class RetrievalException extends RuntimeException {
  public RetrievalException() {
  }

  public RetrievalException(String message) {
    super(message);
  }

  public RetrievalException(String message, Throwable cause) {
    super(message, cause);
  }

  public RetrievalException(Throwable cause) {
    super(cause);
  }
}
