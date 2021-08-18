package com.computablefacts.logfmt;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class TaskLogFormatterTest {

  static void verify(String format, Map<String, Object> params, boolean hasUser) {

    Map<String, String> map = LogFormatter.parse(format);

    Assert.assertEquals(11 + params.size(), map.size());
    Assert.assertFalse(map.get("timestamp").isEmpty());

    // Task
    Assert.assertFalse(map.get("task_id").isEmpty());
    Assert.assertEquals("test_formatter", map.get("task_name"));

    // App
    Assert.assertFalse(map.get("git_head").isEmpty());
    Assert.assertFalse(map.get("git_is_dirty").isEmpty());

    // Env
    Assert.assertEquals("LOCAL", map.get("env"));

    // User
    if (hasUser) {
      Assert.assertEquals("1", map.get("client_id"));
      Assert.assertEquals("ACME", map.get("client_name"));
      Assert.assertEquals("1", map.get("user_id"));
      Assert.assertEquals("jdoe", map.get("user_name"));
      Assert.assertEquals("j.doe@example.com", map.get("user_email"));
    } else {
      Assert.assertEquals("", map.get("client_id"));
      Assert.assertEquals("", map.get("client_name"));
      Assert.assertEquals("", map.get("user_id"));
      Assert.assertEquals("", map.get("user_name"));
      Assert.assertEquals("", map.get("user_email"));
    }

    // Caller-defined
    for (Map.Entry<String, Object> entry : params.entrySet()) {
      Assert.assertEquals(entry.getValue().toString(), map.get(entry.getKey()));
    }
  }

  static Map<String, Object> params(TaskLogFormatter.eLogLevel level) {
    Map<String, Object> params = new HashMap<>();
    params.put("level", level);
    params.put("key1", "value1");
    params.put("key2", "value\t2");
    params.put("key3", 0);
    return params;
  }

  static LogFormatter taskLogFormatterWithUser() {
    return TaskLogFormatter
        .create(new TaskLogFormatter.Task("test_formatter"),
            new TaskLogFormatter.Environment(TaskLogFormatter.eEnv.LOCAL),
            new TaskLogFormatter.User("1", "ACME", "1", "jdoe", "j.doe@example.com"))
        .addGitProperties("git.properties");
  }

  static LogFormatter taskLogFormatterWithoutUser() {
    return TaskLogFormatter
        .create(new TaskLogFormatter.Task("test_formatter"),
            new TaskLogFormatter.Environment(TaskLogFormatter.eEnv.LOCAL))
        .addGitProperties("git.properties");
  }

  @Test
  public void testFatal() {

    verify(
        taskLogFormatterWithoutUser().add(params(TaskLogFormatter.eLogLevel.FATAL)).formatFatal(),
        params(TaskLogFormatter.eLogLevel.FATAL), false);

    verify(taskLogFormatterWithUser().add(params(TaskLogFormatter.eLogLevel.FATAL)).formatFatal(),
        params(TaskLogFormatter.eLogLevel.FATAL), true);
  }

  @Test
  public void testError() {

    verify(
        taskLogFormatterWithoutUser().add(params(TaskLogFormatter.eLogLevel.ERROR)).formatError(),
        params(TaskLogFormatter.eLogLevel.ERROR), false);

    verify(taskLogFormatterWithUser().add(params(TaskLogFormatter.eLogLevel.ERROR)).formatError(),
        params(TaskLogFormatter.eLogLevel.ERROR), true);
  }

  @Test
  public void testWarn() {

    verify(taskLogFormatterWithoutUser().add(params(TaskLogFormatter.eLogLevel.WARN)).formatWarn(),
        params(TaskLogFormatter.eLogLevel.WARN), false);

    verify(taskLogFormatterWithUser().add(params(TaskLogFormatter.eLogLevel.WARN)).formatWarn(),
        params(TaskLogFormatter.eLogLevel.WARN), true);
  }

  @Test
  public void testInfo() {

    verify(taskLogFormatterWithoutUser().add(params(TaskLogFormatter.eLogLevel.INFO)).formatInfo(),
        params(TaskLogFormatter.eLogLevel.INFO), false);

    verify(taskLogFormatterWithUser().add(params(TaskLogFormatter.eLogLevel.INFO)).formatInfo(),
        params(TaskLogFormatter.eLogLevel.INFO), true);
  }

  @Test
  public void testDebug() {

    verify(
        taskLogFormatterWithoutUser().add(params(TaskLogFormatter.eLogLevel.DEBUG)).formatDebug(),
        params(TaskLogFormatter.eLogLevel.DEBUG), false);

    verify(taskLogFormatterWithUser().add(params(TaskLogFormatter.eLogLevel.DEBUG)).formatDebug(),
        params(TaskLogFormatter.eLogLevel.DEBUG), true);
  }

  @Test
  public void testTrace() {

    verify(
        taskLogFormatterWithoutUser().add(params(TaskLogFormatter.eLogLevel.TRACE)).formatTrace(),
        params(TaskLogFormatter.eLogLevel.TRACE), false);

    verify(taskLogFormatterWithUser().add(params(TaskLogFormatter.eLogLevel.TRACE)).formatTrace(),
        params(TaskLogFormatter.eLogLevel.TRACE), true);
  }
}
