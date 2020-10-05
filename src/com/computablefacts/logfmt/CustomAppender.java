package com.computablefacts.logfmt;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import com.google.errorprone.annotations.CheckReturnValue;

@CheckReturnValue
public abstract class CustomAppender extends AppenderSkeleton {

  private static final BlockingQueue<LoggingEvent> queue_ = new LinkedBlockingQueue<>();
  private static CustomAppender instance_;
  private static Thread thread_ = null;

  static {
    thread_ = new Thread(CustomAppender::processQueue);
    thread_.setDaemon(true);
    thread_.start();
  }

  public CustomAppender() {
    super();
    instance_ = this;
  }

  public CustomAppender(boolean isActive) {
    super(isActive);
    instance_ = this;
  }

  private static void processQueue() {
    while (true) {
      try {

        LoggingEvent event = queue_.poll(1L, TimeUnit.SECONDS);

        if (event != null) {
          instance_.processEvent(event);
        }
      } catch (InterruptedException e) {
        // ignore
      }
    }
  }

  @Override
  protected void append(LoggingEvent event) {
    queue_.add(event);
  }

  @Override
  public synchronized void close() {

    if (this.closed) {
      return;
    }

    thread_.interrupt();
    this.closed = true;
  }

  @Override
  public boolean requiresLayout() {
    return false;
  }

  protected abstract void processEvent(LoggingEvent loggingEvent);
}
