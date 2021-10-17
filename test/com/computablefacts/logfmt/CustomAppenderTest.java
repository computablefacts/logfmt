package com.computablefacts.logfmt;

import static com.computablefacts.logfmt.TaskLogFormatterTest.params;
import static com.computablefacts.logfmt.TaskLogFormatterTest.taskLogFormatterWithUserWithGitProperties;
import static com.computablefacts.logfmt.TaskLogFormatterTest.taskLogFormatterWithoutUserWithGitProperties;
import static com.computablefacts.logfmt.TaskLogFormatterTest.verify;

import java.util.Map;

import org.apache.log4j.Appender;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Test;

/**
 * This class is not thread-safe because a {@link CustomAppender} is used. Do not execute methods in
 * parallel.
 */
@net.jcip.annotations.NotThreadSafe
public class CustomAppenderTest {

  @Test
  public void testTrace() {

    loggerWithoutUser(params(TaskLogFormatter.eLogLevel.TRACE))
        .info(taskLogFormatterWithoutUserWithGitProperties()
            .add(params(TaskLogFormatter.eLogLevel.TRACE)).formatTrace());

    loggerWithUser(params(TaskLogFormatter.eLogLevel.TRACE))
        .info(taskLogFormatterWithUserWithGitProperties()
            .add(params(TaskLogFormatter.eLogLevel.TRACE)).formatTrace());
  }

  @Test
  public void testDebug() {

    loggerWithoutUser(params(TaskLogFormatter.eLogLevel.DEBUG))
        .info(taskLogFormatterWithoutUserWithGitProperties()
            .add(params(TaskLogFormatter.eLogLevel.DEBUG)).formatDebug());

    loggerWithUser(params(TaskLogFormatter.eLogLevel.DEBUG))
        .info(taskLogFormatterWithUserWithGitProperties()
            .add(params(TaskLogFormatter.eLogLevel.DEBUG)).formatDebug());
  }

  @Test
  public void testInfo() {

    loggerWithoutUser(params(TaskLogFormatter.eLogLevel.INFO))
        .info(taskLogFormatterWithoutUserWithGitProperties()
            .add(params(TaskLogFormatter.eLogLevel.INFO)).formatInfo());

    loggerWithUser(params(TaskLogFormatter.eLogLevel.INFO))
        .info(taskLogFormatterWithUserWithGitProperties()
            .add(params(TaskLogFormatter.eLogLevel.INFO)).formatInfo());
  }

  @Test
  public void testWarn() {

    loggerWithoutUser(params(TaskLogFormatter.eLogLevel.WARN))
        .info(taskLogFormatterWithoutUserWithGitProperties()
            .add(params(TaskLogFormatter.eLogLevel.WARN)).formatWarn());

    loggerWithUser(params(TaskLogFormatter.eLogLevel.WARN))
        .info(taskLogFormatterWithUserWithGitProperties()
            .add(params(TaskLogFormatter.eLogLevel.WARN)).formatWarn());
  }

  @Test
  public void testError() {

    loggerWithoutUser(params(TaskLogFormatter.eLogLevel.ERROR))
        .info(taskLogFormatterWithoutUserWithGitProperties()
            .add(params(TaskLogFormatter.eLogLevel.ERROR)).formatError());

    loggerWithUser(params(TaskLogFormatter.eLogLevel.ERROR))
        .info(taskLogFormatterWithUserWithGitProperties()
            .add(params(TaskLogFormatter.eLogLevel.ERROR)).formatError());
  }

  @Test
  public void testFatal() {

    loggerWithoutUser(params(TaskLogFormatter.eLogLevel.FATAL))
        .info(taskLogFormatterWithoutUserWithGitProperties()
            .add(params(TaskLogFormatter.eLogLevel.FATAL)).formatFatal());

    loggerWithUser(params(TaskLogFormatter.eLogLevel.FATAL))
        .info(taskLogFormatterWithUserWithGitProperties()
            .add(params(TaskLogFormatter.eLogLevel.FATAL)).formatFatal());
  }

  private Logger loggerWithoutUser(Map<String, Object> params) {
    return logger(new CustomAppender() {

      @Override
      protected void processEvent(LoggingEvent loggingEvent) {
        if (loggingEvent != null) {
          verify(loggingEvent.getRenderedMessage(), params, false, true);
        }
      }
    });
  }

  private Logger loggerWithUser(Map<String, Object> params) {
    return logger(new CustomAppender() {

      @Override
      protected void processEvent(LoggingEvent loggingEvent) {
        if (loggingEvent != null) {
          verify(loggingEvent.getRenderedMessage(), params, true, true);
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
