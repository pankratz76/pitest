package org.pitest.aggregate;

import java.io.Serial;

public class ReportAggregationException extends Exception {

  @Serial
  private static final long serialVersionUID = 1L;

  public ReportAggregationException(final String message) {
    super(message);
  }

  public ReportAggregationException(final String message, final Throwable cause) {
    super(message, cause);
  }

}
