package com.computablefacts.logfmt;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import com.google.errorprone.annotations.Var;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@CheckReturnValue
final public class LogFormatter {

  private static final char SEPARATOR = ' ';
  private static final int KEY_START = 0;
  private static final int KEY_LEN = 1;
  private static final int VAL_START = 2;

  private final Map<String, Object> map_ = new ConcurrentHashMap<>();

  private LogFormatter() {
  }

  public static LogFormatter create() {
    return new LogFormatter();
  }

  public static Map<String, String> parse(String log) {

    Preconditions.checkNotNull(log, "log should not be null");

    char[] line = log.toCharArray();
    @Var ScanState state = ScanState.NEXT;
    Map<String, String> parsed = new HashMap<>();
    @Var boolean quoted = false;
    @Var boolean escaped;
    @Var int[] pos = new int[3];

    for (int i = 0; i < line.length; i++) {

      @Var char b = line[i];

      switch (state) {
        case NEXT:
          if (isChar(b)) {
            state = ScanState.KEY;
            pos[KEY_START] = i;
          } else {
            break;
          }
        case KEY:
          if (b == '=') {

            quoted = false;
            pos[KEY_LEN] = i - pos[KEY_START];
            state = ScanState.VAL;
            i++;

            if (i < line.length) {
              if (line[i] == '"') {
                quoted = true;
                i++;
              }
              pos[VAL_START] = i;
              b = line[i];
            } else {
              break;
            }
          } else if (!isChar(b)) {
            char[] key = slice(pos[KEY_START], (i - pos[KEY_START]), line);
            parsed.put(new String(key), "");
            pos = new int[3];
            state = ScanState.NEXT;
            break;
          }
        case VAL:

          escaped = false;

          if (b == '\\' && i < (line.length - 1)) {
            escaped = true;
            i++;
            b = line[i];
          }

          if (!isChar(b, quoted, escaped)) {

            char[] key = slice(pos[KEY_START], pos[KEY_LEN], line);
            char[] value = slice(pos[VAL_START], (i - pos[VAL_START]), line);

            parsed.put(new String(key), unquote(value));

            state = ScanState.NEXT;
            pos = new int[3];
          }
          break;
      }
    }

    if (parsed.isEmpty() && line.length > 0 && pos[KEY_START] == 0 && pos[KEY_LEN] == 0 && pos[VAL_START] == 0) {
      parsed.put(new String(line), "");
    } else if (pos[KEY_START] + pos[KEY_LEN] > 0) {

      char[] key = slice(pos[KEY_START], pos[KEY_LEN], line);
      char[] value = (pos[VAL_START] > 0) ? slice(pos[VAL_START], line.length - pos[VAL_START], line) : new char[0];

      parsed.put(new String(key), unquote(value));
    }
    return parsed;
  }

  private static boolean isChar(char b) {
    return isChar(b, false, false);
  }

  private static boolean isChar(char b, boolean quoted, boolean escaped) {
    if (!quoted) {
      return b > SEPARATOR && b != '=' && b != '"';
    }
    return b >= SEPARATOR && (b != '=' || escaped) && (b != '"' || escaped);
  }

  private static char[] slice(int start, int len, char[] a) {
    char[] n = new char[len];
    System.arraycopy(a, start, n, 0, len);
    return n;
  }

  private static boolean needsQuoting(String string) {

    for (int i = 0; i < string.length(); i++) {

      char c = string.charAt(i);

      if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || (c == '-' || c == '.'))) {
        return true;
      }
    }
    return false;
  }

  @CanIgnoreReturnValue
  private static StringBuilder quote(StringBuilder builder, String string) {
    if (string == null) {
      return builder.append("null");
    }
    if (string.length() == 0) {
      return builder.append("\"\"");
    }
    if (!needsQuoting(string)) {
      return builder.append(string);
    }

    builder.append('"');

    for (int i = 0; i < string.length(); i++) {

      char c = string.charAt(i);

      switch (c) {
        case '\t':
          builder.append("\\t");
          break;
        case '\b':
          builder.append("\\b");
          break;
        case '\n':
          builder.append("\\n");
          break;
        case '\r':
          builder.append("\\r");
          break;
        case '\f':
          builder.append("\\f");
          break;
        case '\"':
          builder.append("\\\"");
          break;
        case '\\':
          builder.append("\\\\");
          break;
        case '=':
          builder.append("\\=");
          break;
        default:
          builder.append(c);
      }
    }
    return builder.append('"');
  }

  @CanIgnoreReturnValue
  private static String unquote(char[] string) {
    if (string == null) {
      return "";
    }
    if (string.length == 0) {
      return "";
    }

    StringBuilder builder = new StringBuilder(string.length);

    for (int i = 0; i < string.length; i++) {

      char curr = string[i];

      if (curr != '\\' || i + 1 >= string.length) {
        builder.append(curr);
      } else {

        char next = string[++i];

        switch (next) {
          case 't':
            builder.append('\t');
            break;
          case 'b':
            builder.append('\b');
            break;
          case 'n':
            builder.append('\n');
            break;
          case 'r':
            builder.append('\r');
            break;
          case 'f':
            builder.append('\f');
            break;
          case '\"':
            builder.append('"');
            break;
          case '\\':
            builder.append('\\');
            break;
          case '=':
            builder.append('=');
            break;
          default:
            i--;
            builder.append(curr);
        }
      }
    }
    return builder.toString();
  }

  @CanIgnoreReturnValue
  public LogFormatter add(Map<String, Object> values) {
    if (values != null && !values.isEmpty()) {
      for (Map.Entry<String, Object> entry : values.entrySet()) {

        // From the author of ConcurrentHashMap himself (Doug Lea) :
        //
        // The main reason that nulls aren't allowed in ConcurrentMaps (ConcurrentHashMaps,
        // ConcurrentSkipListMaps) is that ambiguities that may be just barely tolerable in
        // non-concurrent maps can't be accommodated. The main one is that if map.get(key) returns
        // null, you can't detect whether the key explicitly maps to null vs the key isn't mapped.
        // In a non-concurrent map, you can check this via map.contains(key), but in a concurrent
        // one, the map might have changed between calls.
        if (entry.getKey() != null && entry.getValue() != null) {
          add(entry.getKey(), entry.getValue());
        }
      }
    }
    return this;
  }

  @CanIgnoreReturnValue
  public LogFormatter add(String key, Object value) {
    if (!Strings.isNullOrEmpty(key)) {
      if (key.toLowerCase().contains("password")) {
        map_.put(key, "******");
      } else {
        map_.put(key, value == null ? "null" : value);
      }
    }
    return this;
  }

  public String formatTrace() {
    return format(eLogLevel.TRACE);
  }

  public String formatDebug() {
    return format(eLogLevel.DEBUG);
  }

  public String formatInfo() {
    return format(eLogLevel.INFO);
  }

  public String formatWarn() {
    return format(eLogLevel.WARN);
  }

  public String formatError() {
    return format(eLogLevel.ERROR);
  }

  public String formatFatal() {
    return format(eLogLevel.FATAL);
  }

  @CanIgnoreReturnValue
  public LogFormatter message(String msg) {
    return add("msg", msg);
  }

  @CanIgnoreReturnValue
  public LogFormatter message(Throwable throwable) {
    return add("msg", throwable);
  }

  public String format() {

    StringBuilder builder = new StringBuilder();

    map_.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEachOrdered(entry -> {

      String key = entry.getKey();
      Object value = entry.getValue();

      if (builder.length() > 0) {
        builder.append(' ');
      }

      builder.append(key).append('=');

      if (value instanceof Instant) {
        builder.append(((Instant) value).truncatedTo(ChronoUnit.SECONDS).toString());
      } else if (value instanceof Date) {
        Instant instant = ((Date) value).toInstant().truncatedTo(ChronoUnit.SECONDS);
        builder.append(instant.toString());
      } else {

        String string;

        if (value instanceof String) {
          string = (String) value;
        } else if (value instanceof Throwable) {
          string = Throwables.getStackTraceAsString(Throwables.getRootCause((Throwable) value));
        } else {
          string = value.toString();
        }

        quote(builder, string);
      }
    });

    map_.clear();

    return builder.toString();
  }

  protected String format(eLogLevel level) {
    return this.add("timestamp", Instant.now().toString())
        .add("level", Preconditions.checkNotNull(level, "level should not be null").toString()).format();
  }

  enum eLogLevel {
    TRACE, DEBUG, INFO, WARN, ERROR, FATAL
  }

  private enum ScanState {
    NEXT, KEY, VAL
  }
}
