package com.computablefacts.logfmt;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class TaskLogFormatterTest {

  static void verify(String format, Map<String, Object> params) {

    Map<String, String> map = LogFormatter.parse(format);

    Assert.assertEquals(14 + params.size(), map.size());
    Assert.assertFalse(map.get("timestamp").isEmpty());

    // Task
    Assert.assertFalse(map.get("task_id").isEmpty());
    Assert.assertEquals("test_formatter", map.get("task_name"));

    // App
    Assert.assertFalse(map.get("git_build_version").isEmpty());
    Assert.assertFalse(map.get("git_origin").isEmpty());
    Assert.assertFalse(map.get("git_branch").isEmpty());
    Assert.assertFalse(map.get("git_head").isEmpty());
    Assert.assertFalse(map.get("git_is_dirty").isEmpty());

    // Env
    Assert.assertEquals("LOCAL", map.get("env"));

    // User
    Assert.assertEquals("", map.get("client_id"));
    Assert.assertEquals("", map.get("client_name"));
    Assert.assertEquals("", map.get("user_id"));
    Assert.assertEquals("", map.get("user_name"));
    Assert.assertEquals("", map.get("user_email"));

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

  static TaskLogFormatter taskLogFormatter() {
    return TaskLogFormatter.create("git-logfmt.properties",
        new TaskLogFormatter.Task("test_formatter"),
        new TaskLogFormatter.Environment(TaskLogFormatter.eEnv.LOCAL));
  }

  @Test
  public void testSimpleFatal() {
    verify(taskLogFormatter().add(params(TaskLogFormatter.eLogLevel.FATAL)).formatFatal(),
        params(TaskLogFormatter.eLogLevel.FATAL));
  }

  @Test
  public void testSimpleError() {
    verify(taskLogFormatter().add(params(TaskLogFormatter.eLogLevel.ERROR)).formatError(),
        params(TaskLogFormatter.eLogLevel.ERROR));
  }

  @Test
  public void testSimpleWarn() {
    verify(taskLogFormatter().add(params(TaskLogFormatter.eLogLevel.WARN)).formatWarn(),
        params(TaskLogFormatter.eLogLevel.WARN));
  }

  @Test
  public void testSimpleInfo() {
    verify(taskLogFormatter().add(params(TaskLogFormatter.eLogLevel.INFO)).formatInfo(),
        params(TaskLogFormatter.eLogLevel.INFO));
  }

  @Test
  public void testSimpleDebug() {
    verify(taskLogFormatter().add(params(TaskLogFormatter.eLogLevel.DEBUG)).formatDebug(),
        params(TaskLogFormatter.eLogLevel.DEBUG));
  }

  @Test
  public void testSimpleTrace() {
    verify(taskLogFormatter().add(params(TaskLogFormatter.eLogLevel.TRACE)).formatTrace(),
        params(TaskLogFormatter.eLogLevel.TRACE));
  }
}
