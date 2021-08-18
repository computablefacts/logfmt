package com.computablefacts.logfmt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.log4j.Level;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import com.google.errorprone.annotations.Var;

@Deprecated
@CheckReturnValue
public abstract class LogFormatterHttpAppender extends CustomAppender {

  public LogFormatterHttpAppender() {}

  public LogFormatterHttpAppender(boolean isActive) {
    super(isActive);
  }

  @Override
  protected void processEvent(LoggingEvent loggingEvent) {
    if (loggingEvent != null) {

      LocationInfo locationInfo = loggingEvent.getLocationInformation();

      LogFormatter logFormatter = LogFormatter.create().add("class", locationInfo.getClassName())
          .add("method", locationInfo.getMethodName()).add("file", locationInfo.getFileName())
          .add("line_number", locationInfo.getLineNumber());

      String log = loggingEvent.getRenderedMessage();

      // Ensure the log uses the logfmt format and has at least a timestamp and a level
      if (log.contains("timestamp=") && log.contains("level=")) {
        post(url(), logFormatter.format() + " " + log);
      } else if (loggingEvent.getLevel().equals(Level.DEBUG)) {
        post(url(), logFormatter.message(log).formatDebug());
      } else if (loggingEvent.getLevel().equals(Level.INFO)) {
        post(url(), logFormatter.message(log).formatInfo());
      } else if (loggingEvent.getLevel().equals(Level.WARN)) {
        post(url(), logFormatter.message(log).formatWarn());
      } else if (loggingEvent.getLevel().equals(Level.ERROR)) {
        post(url(), logFormatter.message(log).formatError());
      } else if (loggingEvent.getLevel().equals(Level.FATAL)) {
        post(url(), logFormatter.message(log).formatFatal());
      } else {
        post(url(), logFormatter.message(log).formatTrace());
      }
    }
  }

  /**
   * Returns the HTTP endpoint where the log should be sent.
   *
   * @return URL
   */
  protected abstract String url();

  @CanIgnoreReturnValue
  protected String post(String requestUrl, String payload) {

    Preconditions.checkNotNull(requestUrl, "requestUrl should not be null");
    Preconditions.checkNotNull(payload, "payload should not be null");

    try {

      URL url = new URL(requestUrl);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();

      connection.setDoInput(true);
      connection.setDoOutput(true);
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Accept", "application/json");
      connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

      OutputStreamWriter writer =
          new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8);
      writer.write(payload);
      writer.close();

      @Var
      String responseString;

      try {
        responseString = readInputStream(connection.getInputStream());
      } catch (IOException e) {
        responseString = readInputStream(connection.getErrorStream());
      }

      connection.disconnect();

      return responseString;
    } catch (IOException e) {
      System.out.println(Throwables.getStackTraceAsString(Throwables.getRootCause(e)));
    }
    return "";
  }

  protected String readInputStream(InputStream inputStream) throws IOException {

    if (inputStream == null) {
      return "";
    }

    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
    StringBuilder responseString = new StringBuilder();

    @Var
    String line;

    while ((line = bufferedReader.readLine()) != null) {
      responseString.append(line);
    }

    bufferedReader.close();
    return responseString.toString();
  }
}
