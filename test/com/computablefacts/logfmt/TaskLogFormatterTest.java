package com.computablefacts.logfmt;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class TaskLogFormatterTest {

  public static void verify(String format, Map<String, Object> params, boolean hasUser,
      boolean hasGitProperties) {

    Map<String, String> map = LogFormatter.parse(format);

    if (hasGitProperties) {
      Assert.assertEquals(11 + params.size() - 2 /* null key and null value */, map.size());
    } else {
      Assert.assertEquals(9 + params.size() - 2 /* null key and null value */, map.size());
    }

    Assert.assertFalse(map.get("timestamp").isEmpty());

    // Task
    Assert.assertFalse(map.get("task_id").isEmpty());
    Assert.assertEquals("test_formatter", map.get("task_name"));

    // App
    if (hasGitProperties) {
      Assert.assertFalse(map.get("git_head").isEmpty());
      Assert.assertFalse(map.get("git_is_dirty").isEmpty());
    } else {
      Assert.assertNull(map.get("git_head"));
      Assert.assertNull(map.get("git_is_dirty"));
    }

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
      if (!"null_key".equals(entry.getValue()) && !"null_value".equals(entry.getKey())) {
        Assert.assertEquals(entry.getValue().toString(), map.get(entry.getKey()));
      }
    }
  }

  public static Map<String, Object> params(TaskLogFormatter.eLogLevel level) {
    Map<String, Object> params = new HashMap<>();
    params.put("level", level);
    params.put("key1", "value1");
    params.put("key2", "value\t2");
    params.put("key3", 0);
    params.put("null_value", null);
    params.put(null, "null_key");
    return params;
  }

  public static LogFormatter taskLogFormatterWithUserWithGitProperties() {
    return TaskLogFormatter.create(new TaskLogFormatter.Task("test_formatter"),
        new TaskLogFormatter.Environment(TaskLogFormatter.eEnv.LOCAL),
        new TaskLogFormatter.User("1", "ACME", "1", "jdoe", "j.doe@example.com"), true);
  }

  public static LogFormatter taskLogFormatterWithoutUserWithGitProperties() {
    return TaskLogFormatter.create(new TaskLogFormatter.Task("test_formatter"),
        new TaskLogFormatter.Environment(TaskLogFormatter.eEnv.LOCAL), true);
  }

  public static LogFormatter taskLogFormatterWithUserWithoutGitProperties() {
    return TaskLogFormatter.create(new TaskLogFormatter.Task("test_formatter"),
        new TaskLogFormatter.Environment(TaskLogFormatter.eEnv.LOCAL),
        new TaskLogFormatter.User("1", "ACME", "1", "jdoe", "j.doe@example.com"));
  }

  public static LogFormatter taskLogFormatterWithoutUserWithoutGitProperties() {
    return TaskLogFormatter.create(new TaskLogFormatter.Task("test_formatter"),
        new TaskLogFormatter.Environment(TaskLogFormatter.eEnv.LOCAL));
  }

  @Test
  public void testFatal() {

    verify(taskLogFormatterWithoutUserWithGitProperties()
        .add(params(TaskLogFormatter.eLogLevel.FATAL)).formatFatal(),
        params(TaskLogFormatter.eLogLevel.FATAL), false, true);

    verify(taskLogFormatterWithUserWithGitProperties().add(params(TaskLogFormatter.eLogLevel.FATAL))
        .formatFatal(), params(TaskLogFormatter.eLogLevel.FATAL), true, true);
  }

  @Test
  public void testError() {

    verify(taskLogFormatterWithoutUserWithGitProperties()
        .add(params(TaskLogFormatter.eLogLevel.ERROR)).formatError(),
        params(TaskLogFormatter.eLogLevel.ERROR), false, true);

    verify(taskLogFormatterWithUserWithGitProperties().add(params(TaskLogFormatter.eLogLevel.ERROR))
        .formatError(), params(TaskLogFormatter.eLogLevel.ERROR), true, true);
  }

  @Test
  public void testWarn() {

    verify(taskLogFormatterWithoutUserWithGitProperties()
        .add(params(TaskLogFormatter.eLogLevel.WARN)).formatWarn(),
        params(TaskLogFormatter.eLogLevel.WARN), false, true);

    verify(taskLogFormatterWithUserWithGitProperties().add(params(TaskLogFormatter.eLogLevel.WARN))
        .formatWarn(), params(TaskLogFormatter.eLogLevel.WARN), true, true);
  }

  @Test
  public void testInfo() {

    verify(taskLogFormatterWithoutUserWithGitProperties()
        .add(params(TaskLogFormatter.eLogLevel.INFO)).formatInfo(),
        params(TaskLogFormatter.eLogLevel.INFO), false, true);

    verify(taskLogFormatterWithUserWithGitProperties().add(params(TaskLogFormatter.eLogLevel.INFO))
        .formatInfo(), params(TaskLogFormatter.eLogLevel.INFO), true, true);
  }

  @Test
  public void testDebug() {

    verify(taskLogFormatterWithoutUserWithGitProperties()
        .add(params(TaskLogFormatter.eLogLevel.DEBUG)).formatDebug(),
        params(TaskLogFormatter.eLogLevel.DEBUG), false, true);

    verify(taskLogFormatterWithUserWithGitProperties().add(params(TaskLogFormatter.eLogLevel.DEBUG))
        .formatDebug(), params(TaskLogFormatter.eLogLevel.DEBUG), true, true);
  }

  @Test
  public void testTrace() {

    verify(taskLogFormatterWithoutUserWithGitProperties()
        .add(params(TaskLogFormatter.eLogLevel.TRACE)).formatTrace(),
        params(TaskLogFormatter.eLogLevel.TRACE), false, true);

    verify(taskLogFormatterWithUserWithGitProperties().add(params(TaskLogFormatter.eLogLevel.TRACE))
        .formatTrace(), params(TaskLogFormatter.eLogLevel.TRACE), true, true);
  }

  @Test
  public void testFatalWithoutGitProperties() {

    verify(
        taskLogFormatterWithoutUserWithoutGitProperties()
            .add(params(TaskLogFormatter.eLogLevel.FATAL)).formatFatal(),
        params(TaskLogFormatter.eLogLevel.FATAL), false, false);

    verify(taskLogFormatterWithUserWithoutGitProperties()
        .add(params(TaskLogFormatter.eLogLevel.FATAL)).formatFatal(),
        params(TaskLogFormatter.eLogLevel.FATAL), true, false);
  }

  @Test
  public void testErrorWithoutGitProperties() {

    verify(
        taskLogFormatterWithoutUserWithoutGitProperties()
            .add(params(TaskLogFormatter.eLogLevel.ERROR)).formatError(),
        params(TaskLogFormatter.eLogLevel.ERROR), false, false);

    verify(taskLogFormatterWithUserWithoutGitProperties()
        .add(params(TaskLogFormatter.eLogLevel.ERROR)).formatError(),
        params(TaskLogFormatter.eLogLevel.ERROR), true, false);
  }

  @Test
  public void testWarnWithoutGitProperties() {

    verify(
        taskLogFormatterWithoutUserWithoutGitProperties()
            .add(params(TaskLogFormatter.eLogLevel.WARN)).formatWarn(),
        params(TaskLogFormatter.eLogLevel.WARN), false, false);

    verify(taskLogFormatterWithUserWithoutGitProperties()
        .add(params(TaskLogFormatter.eLogLevel.WARN)).formatWarn(),
        params(TaskLogFormatter.eLogLevel.WARN), true, false);
  }

  @Test
  public void testInfoWithoutGitProperties() {

    verify(
        taskLogFormatterWithoutUserWithoutGitProperties()
            .add(params(TaskLogFormatter.eLogLevel.INFO)).formatInfo(),
        params(TaskLogFormatter.eLogLevel.INFO), false, false);

    verify(taskLogFormatterWithUserWithoutGitProperties()
        .add(params(TaskLogFormatter.eLogLevel.INFO)).formatInfo(),
        params(TaskLogFormatter.eLogLevel.INFO), true, false);
  }

  @Test
  public void testDebugWithoutGitProperties() {

    verify(
        taskLogFormatterWithoutUserWithoutGitProperties()
            .add(params(TaskLogFormatter.eLogLevel.DEBUG)).formatDebug(),
        params(TaskLogFormatter.eLogLevel.DEBUG), false, false);

    verify(taskLogFormatterWithUserWithoutGitProperties()
        .add(params(TaskLogFormatter.eLogLevel.DEBUG)).formatDebug(),
        params(TaskLogFormatter.eLogLevel.DEBUG), true, false);
  }

  @Test
  public void testTraceWithoutGitProperties() {

    verify(
        taskLogFormatterWithoutUserWithoutGitProperties()
            .add(params(TaskLogFormatter.eLogLevel.TRACE)).formatTrace(),
        params(TaskLogFormatter.eLogLevel.TRACE), false, false);

    verify(taskLogFormatterWithUserWithoutGitProperties()
        .add(params(TaskLogFormatter.eLogLevel.TRACE)).formatTrace(),
        params(TaskLogFormatter.eLogLevel.TRACE), true, false);
  }
}
