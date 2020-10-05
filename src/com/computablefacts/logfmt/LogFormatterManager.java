package com.computablefacts.logfmt;

import com.google.errorprone.annotations.CheckReturnValue;

@CheckReturnValue
final public class LogFormatterManager {

  private final static ThreadLocal<LogFormatter> logFormatter_ = new ThreadLocal<>();

  public LogFormatterManager() {}

  public static LogFormatter logFormatter() {
    return logFormatter(null);
  }

  public static LogFormatter logFormatter(LogFormatter logFormatter) {
    if (logFormatter == null) {
      if (logFormatter_.get() == null) {
        logFormatter_.set(LogFormatter.create());
      }
    } else {
      logFormatter_.set(logFormatter);
    }
    return logFormatter_.get();
  }
}
