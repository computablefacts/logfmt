package com.computablefacts.logfmt;

import static com.computablefacts.logfmt.TaskLogFormatterTest.params;
import static com.computablefacts.logfmt.TaskLogFormatterTest.taskLogFormatter;
import static com.computablefacts.logfmt.TaskLogFormatterTest.verify;

import java.util.Map;

import org.apache.log4j.Appender;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Test;

public class CustomAppenderTest {

  @Test
  public void testAppender() {
    logger(params(TaskLogFormatter.eLogLevel.INFO))
        .info(taskLogFormatter().add(params(TaskLogFormatter.eLogLevel.INFO)).formatInfo());
  }

  private Logger logger(Map<String, Object> params) {
    return logger(new CustomAppender() {

      @Override
      protected void processEvent(LoggingEvent loggingEvent) {
        if (loggingEvent != null) {
          verify(loggingEvent.getRenderedMessage(), params);
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
