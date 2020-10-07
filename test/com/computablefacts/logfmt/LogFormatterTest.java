package com.computablefacts.logfmt;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Test;

public class LogFormatterTest {

  @Test
  public void testNull() {

    String log = LogFormatter.create().add("msg", null).format();

    Assert.assertEquals("msg=null", log);

    Map<String, String> map = LogFormatter.parse(log);

    Assert.assertEquals(1, map.size());
    Assert.assertEquals("null", map.get("msg"));
  }

  @Test
  public void testDoubleQuotes() {

    String log =
        LogFormatter.create().add("msg", "Message with \"double quotes\" inside.").format();

    Assert.assertEquals("msg=\"Message with \\\"double quotes\\\" inside.\"", log);

    Map<String, String> map = LogFormatter.parse(log);

    Assert.assertEquals(1, map.size());
    Assert.assertEquals("Message with \"double quotes\" inside.", map.get("msg"));
  }

  @Test
  public void testInstant() {

    Instant now = Instant.parse("2017-11-30T15:10:25Z");

    String log = LogFormatter.create().add("msg", now).format();

    Assert.assertEquals("msg=2017-11-30T15:10:25Z", log);

    Map<String, String> map = LogFormatter.parse(log);

    Assert.assertEquals(1, map.size());
    Assert.assertEquals("2017-11-30T15:10:25Z", map.get("msg"));
  }

  @Test
  public void testDate() {

    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    calendar.set(2017, Calendar.NOVEMBER, 30, 15, 10, 25);

    Date date = calendar.getTime();

    String log = LogFormatter.create().add("msg", date).format();

    Assert.assertEquals("msg=2017-11-30T15:10:25Z", log);

    Map<String, String> map = LogFormatter.parse(log);

    Assert.assertEquals(1, map.size());
    Assert.assertEquals("2017-11-30T15:10:25Z", map.get("msg"));
  }

  @Test
  public void testInteger() {

    String log = LogFormatter.create().add("port", 443).format();

    Assert.assertEquals("port=443", log);

    Map<String, String> map = LogFormatter.parse(log);

    Assert.assertEquals(1, map.size());
    Assert.assertEquals("443", map.get("port"));
  }

  @Test
  public void testLong() {

    String log = LogFormatter.create().add("msg", 443L).format();

    Assert.assertEquals("msg=443", log);

    Map<String, String> map = LogFormatter.parse(log);

    Assert.assertEquals(1, map.size());
    Assert.assertEquals("443", map.get("msg"));
  }

  @Test
  public void testDouble() {

    String log = LogFormatter.create().add("msg", 123.456d).format();

    Assert.assertEquals("msg=123.456", log);

    Map<String, String> map = LogFormatter.parse(log);

    Assert.assertEquals(1, map.size());
    Assert.assertEquals("123.456", map.get("msg"));
  }

  @Test
  public void testFloat() {

    String log = LogFormatter.create().add("msg", 123.456f).format();

    Assert.assertEquals("msg=123.456", log);

    Map<String, String> map = LogFormatter.parse(log);

    Assert.assertEquals(1, map.size());
    Assert.assertEquals("123.456", map.get("msg"));
  }

  @Test
  public void testBoolean() {

    String log = LogFormatter.create().add("msg", true).format();

    Assert.assertEquals("msg=true", log);

    Map<String, String> map = LogFormatter.parse(log);

    Assert.assertEquals(1, map.size());
    Assert.assertEquals("true", map.get("msg"));
  }

  @Test
  public void testException() {

    String log = LogFormatter.create().add("msg", new Exception("Custom exception!")).format();

    Assert.assertTrue(log.contains("Custom exception!"));

    Map<String, String> map = LogFormatter.parse(log);

    Assert.assertEquals(1, map.size());
    Assert.assertTrue(map.get("msg").contains("Custom exception!"));
  }

  @Test
  public void testFormFeed() {

    String log = LogFormatter.create().add("msg", "\f").format();

    Assert.assertEquals("msg=\"\\f\"", log);

    Map<String, String> map = LogFormatter.parse(log);

    Assert.assertEquals(1, map.size());
    Assert.assertEquals("\f", map.get("msg"));
  }

  @Test
  public void testBackslash() {

    String log = LogFormatter.create().add("msg", "\\").format();

    Assert.assertEquals("msg=\"\\\\\"", log);

    Map<String, String> map = LogFormatter.parse(log);

    Assert.assertEquals(1, map.size());
    Assert.assertEquals("\\", map.get("msg"));
  }

  @Test
  public void testWordBoundary() {

    String log = LogFormatter.create().add("msg", "\b").format();

    Assert.assertEquals("msg=\"\\b\"", log);

    Map<String, String> map = LogFormatter.parse(log);

    Assert.assertEquals(1, map.size());
    Assert.assertEquals("\b", map.get("msg"));
  }

  @Test
  public void testLogFmt() {

    String log1 = LogFormatter.create().add("key1", "value1").add("key2", "value 2")
        .add("key3", "Hello \"world\"!\nHello \"world\"!").add("key4", 4).format();
    String log2 = LogFormatter.create().message(log1).format();

    Map<String, String> map = LogFormatter.parse(log2);

    Assert.assertEquals(1, map.size());
    Assert.assertEquals(log1, map.get("msg"));
  }

  @Test
  public void testMultipleKeys() {

    String log = LogFormatter.create().add("key1", "value1").add("key2", "value 2")
        .add("key3", "Hello \"world\"!\nHello \"world\"!").add("key4", 4).format();

    Assert.assertEquals(
        "key1=value1 key2=\"value 2\" key3=\"Hello \\\"world\\\"!\\nHello \\\"world\\\"!\" key4=4",
        log);

    Map<String, String> map = LogFormatter.parse(log);

    Assert.assertEquals(4, map.size());
    Assert.assertEquals("value1", map.get("key1"));
    Assert.assertEquals("value 2", map.get("key2"));
    Assert.assertEquals("Hello \"world\"!\nHello \"world\"!", map.get("key3"));
    Assert.assertEquals("4", map.get("key4"));
  }

  @Test
  public void testNullKey() {

    String log = LogFormatter.create().add(null, "msg").format();

    Assert.assertEquals("", log);

    Map<String, String> map = LogFormatter.parse(log);

    Assert.assertEquals(0, map.size());
  }

  @Test
  public void testLargeString() {

    String log = LogFormatter.create().add("msg",
        "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.")
        .format();

    Assert.assertEquals(
        "msg=\"Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem...\"",
        log);

    Map<String, String> map = LogFormatter.parse(log);

    Assert.assertEquals(1, map.size());
    Assert.assertEquals(
        "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem...",
        map.get("msg"));
  }

  @Test
  public void testKeyWithoutValue() {

    Map<String, String> map = LogFormatter.parse("key=");

    Assert.assertEquals(1, map.size());
    Assert.assertEquals("", map.get("key"));
  }

  @Test
  public void testKeyWithoutValueAndEqual1() {

    Map<String, String> map = LogFormatter.parse("key");

    Assert.assertEquals(1, map.size());
    Assert.assertEquals("", map.get("key"));
  }

  @Test
  public void testKeyWithoutValueAndEqual2() {

    Map<String, String> map = LogFormatter.parse("key1=val1 key2 key3=val3");

    Assert.assertEquals(3, map.size());
    Assert.assertEquals("val1", map.get("key1"));
    Assert.assertEquals("", map.get("key2"));
    Assert.assertEquals("val3", map.get("key3"));
  }

  @Test
  public void testKeyWithSpecialCharacters1() {

    Map<String, String> map = LogFormatter.parse("%^asdf");

    Assert.assertEquals(1, map.size());
    Assert.assertEquals("", map.get("%^asdf"));
  }

  @Test
  public void testKeyWithSpecialCharacters2() {

    Map<String, String> map = LogFormatter.parse("%^asdf=test");

    Assert.assertEquals(1, map.size());
    Assert.assertEquals("test", map.get("%^asdf"));
  }

  @Test
  public void testTrimWhitespaces() {

    Map<String, String> map =
        LogFormatter.parse("foo=bar a=1\\4 baz=\"hello kitty\" cool%story=bro f %^asdf  ");

    Assert.assertEquals(6, map.size());
    Assert.assertEquals("bar", map.get("foo"));
    Assert.assertEquals("1\\4", map.get("a"));
    Assert.assertEquals("hello kitty", map.get("baz"));
    Assert.assertEquals("bro", map.get("cool%story"));
    Assert.assertEquals("", map.get("f"));
    Assert.assertEquals("", map.get("%^asdf"));
  }

  @Test
  public void testAddMessage() {

    String msg = "Java package for generating and parsing log lines in the logfmt style.";
    LogFormatter lf = LogFormatter.create().message(msg);

    Assert.assertTrue(lf.formatTrace().contains("msg=\"" + msg + "\""));
  }

  @Test
  public void testAddException() {

    LogFormatter lf = LogFormatter.create().message(new NullPointerException("My custom message."));

    Assert.assertTrue(
        lf.formatTrace().contains("msg=\"java.lang.NullPointerException: My custom message."));
  }

  @Test
  public void testMissingGitProperties() {

    LogFormatter lf = LogFormatter.create().addGitProperties("missing-file.properties")
        .message("My custom message.");

    Assert.assertTrue(lf.formatTrace().contains("msg=\"My custom message."));
  }
}
