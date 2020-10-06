package com.computablefacts.logfmt;

import static com.computablefacts.logfmt.TaskLogFormatterTest.params;
import static com.computablefacts.logfmt.TaskLogFormatterTest.taskLogFormatterWithUser;
import static com.computablefacts.logfmt.TaskLogFormatterTest.taskLogFormatterWithoutUser;
import static com.computablefacts.logfmt.TaskLogFormatterTest.verify;

import java.util.Map;

import org.apache.log4j.Appender;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Test;

public class CustomAppenderTest {

  @Test
  public void testTrace() {

    loggerWithoutUser(params(TaskLogFormatter.eLogLevel.TRACE)).info(
        taskLogFormatterWithoutUser().add(params(TaskLogFormatter.eLogLevel.TRACE)).formatTrace());

    loggerWithUser(params(TaskLogFormatter.eLogLevel.TRACE)).info(
        taskLogFormatterWithUser().add(params(TaskLogFormatter.eLogLevel.TRACE)).formatTrace());
  }

  @Test
  public void testDebug() {

    loggerWithoutUser(params(TaskLogFormatter.eLogLevel.DEBUG)).info(
        taskLogFormatterWithoutUser().add(params(TaskLogFormatter.eLogLevel.DEBUG)).formatDebug());

    loggerWithUser(params(TaskLogFormatter.eLogLevel.DEBUG)).info(
        taskLogFormatterWithUser().add(params(TaskLogFormatter.eLogLevel.DEBUG)).formatDebug());
  }

  @Test
  public void testInfo() {

    loggerWithoutUser(params(TaskLogFormatter.eLogLevel.INFO)).info(
        taskLogFormatterWithoutUser().add(params(TaskLogFormatter.eLogLevel.INFO)).formatInfo());

    loggerWithUser(params(TaskLogFormatter.eLogLevel.INFO))
        .info(taskLogFormatterWithUser().add(params(TaskLogFormatter.eLogLevel.INFO)).formatInfo());
  }

  @Test
  public void testWarn() {

    loggerWithoutUser(params(TaskLogFormatter.eLogLevel.WARN)).info(
        taskLogFormatterWithoutUser().add(params(TaskLogFormatter.eLogLevel.WARN)).formatWarn());

    loggerWithUser(params(TaskLogFormatter.eLogLevel.WARN))
        .info(taskLogFormatterWithUser().add(params(TaskLogFormatter.eLogLevel.WARN)).formatWarn());
  }

  @Test
  public void testError() {

    loggerWithoutUser(params(TaskLogFormatter.eLogLevel.ERROR)).info(
        taskLogFormatterWithoutUser().add(params(TaskLogFormatter.eLogLevel.ERROR)).formatError());

    loggerWithUser(params(TaskLogFormatter.eLogLevel.ERROR)).info(
        taskLogFormatterWithUser().add(params(TaskLogFormatter.eLogLevel.ERROR)).formatError());
  }

  @Test
  public void testFatal() {

    loggerWithoutUser(params(TaskLogFormatter.eLogLevel.FATAL)).info(
        taskLogFormatterWithoutUser().add(params(TaskLogFormatter.eLogLevel.FATAL)).formatFatal());

    loggerWithUser(params(TaskLogFormatter.eLogLevel.FATAL)).info(
        taskLogFormatterWithUser().add(params(TaskLogFormatter.eLogLevel.FATAL)).formatFatal());
  }

  private Logger loggerWithoutUser(Map<String, Object> params) {
    return logger(new CustomAppender() {

      @Override
      protected void processEvent(LoggingEvent loggingEvent) {
        if (loggingEvent != null) {
          verify(loggingEvent.getRenderedMessage(), params, false);
        }
      }
    });
  }

  private Logger loggerWithUser(Map<String, Object> params) {
    return logger(new CustomAppender() {

      @Override
      protected void processEvent(LoggingEvent loggingEvent) {
        if (loggingEvent != null) {
          verify(loggingEvent.getRenderedMessage(), params, true);
        }
      }
    });
  }

  private Logger logger(Appender appender) {
    Logger logger = LogManager.getLogger(CustomAppenderTest.class);
    logger.removeAllAppenders();
    logger.addAppender(appender);
    return logger;
  }
}
