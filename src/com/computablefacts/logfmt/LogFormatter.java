package com.computablefacts.logfmt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import com.google.errorprone.annotations.Var;

@CheckReturnValue
public class LogFormatter {

  private static final ObjectMapper mapper_ = new ObjectMapper();

  private static final char SEPARATOR = ' ';
  private static final int KEY_START = 0;
  private static final int KEY_LEN = 1;
  private static final int VAL_START = 2;

  private final Map<String, Object> map_ = new ConcurrentHashMap<>();

  protected LogFormatter() {}

  public static LogFormatter create(boolean hasGitProperties) {
    return hasGitProperties ? new LogFormatter().addGitProperties("git.properties")
        : new LogFormatter();
  }

  public static LogFormatter create() {
    return new LogFormatter();
  }

  public static Map<String, String> parse(String log) {

    Preconditions.checkNotNull(log, "log should not be null");

    char[] line = log.toCharArray();
    @Var
    ScanState state = ScanState.NEXT;
    Map<String, String> parsed = new HashMap<>();
    @Var
    boolean quoted = false;
    @Var
    boolean escaped;
    @Var
    int[] pos = new int[3];

    for (int i = 0; i < line.length; i++) {

      @Var
      char b = line[i];

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

    if (parsed.isEmpty() && line.length > 0 && pos[KEY_START] == 0 && pos[KEY_LEN] == 0
        && pos[VAL_START] == 0) {
      parsed.put(new String(line), "");
    } else if (pos[KEY_START] + pos[KEY_LEN] > 0) {

      char[] key = slice(pos[KEY_START], pos[KEY_LEN], line);
      char[] value =
          (pos[VAL_START] > 0) ? slice(pos[VAL_START], line.length - pos[VAL_START], line)
              : new char[0];

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

      if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
          || (c == '-' || c == '.'))) {
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

  private static Map<String, Object> asObject(String json) {
    try {
      return json == null ? Collections.emptyMap()
          : mapper_.readValue(json, TypeFactory.defaultInstance().constructType(Map.class));
    } catch (IOException e) {
      // TODO
    }
    return Collections.emptyMap();
  }

  @CanIgnoreReturnValue
  public LogFormatter add(Map<String, Object> values) {
    if (values != null && !values.isEmpty()) {
      map_.putAll(values);
    }
    return this;
  }

  @CanIgnoreReturnValue
  public LogFormatter add(String key, Object value) {
    if (!Strings.isNullOrEmpty(key)) {
      map_.put(key, value == null ? "null" : value);
    }
    return this;
  }

  @CanIgnoreReturnValue
  public LogFormatter addGitProperties(String properties) {

    Preconditions.checkNotNull(properties, "properties should not be null");

    Map<String, Object> gitProperties = loadGitProperties(properties);

    String gitOrigin = (String) gitProperties.getOrDefault("git.remote.origin.url", "");
    String gitHead = (String) gitProperties.getOrDefault("git.commit.id.abbrev", "");
    boolean gitIsDirty = Boolean.parseBoolean((String) gitProperties.getOrDefault("git.dirty", ""));

    return add("git_origin", gitOrigin).add("git_head", gitHead).add("git_is_dirty", gitIsDirty);
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

  public synchronized String format() {

    StringBuilder builder = new StringBuilder();

    for (Map.Entry<String, Object> entry : map_.entrySet()) {

      String key = entry.getKey();
      Object value = entry.getValue();

      if (key == null) {
        continue;
      }

      if (builder.length() > 0) {
        builder.append(' ');
      }

      builder.append(key).append('=');

      if (value == null) {
        builder.append("null");
      } else if (value instanceof Instant) {
        builder.append(((Instant) value).truncatedTo(ChronoUnit.SECONDS).toString());
      } else if (value instanceof Date) {
        Instant instant = ((Date) value).toInstant().truncatedTo(ChronoUnit.SECONDS);
        builder.append(instant.toString());
      } else {

        String string;

        if (value instanceof String) {
          if (((String) value).length() > 80) {
            string = ((String) value).substring(0, 80) + "...";
          } else {
            string = (String) value;
          }
        } else if (value instanceof Throwable) {
          string = Throwables.getStackTraceAsString(Throwables.getRootCause((Throwable) value));
        } else {
          string = value.toString();
        }

        quote(builder, string);
      }
    }

    map_.clear();

    return builder.toString();
  }

  protected synchronized String format(eLogLevel level) {
    return this.add("timestamp", Instant.now().toString())
        .add("level", Preconditions.checkNotNull(level, "level should not be null").toString())
        .format();
  }

  /**
   * Maven's Git commit id must be set :
   *
   * <pre>
   *   <plugin>
   *       <groupId>pl.project13.maven</groupId>
   *       <version>${git-commit-id-plugin.version}</version>
   *       <artifactId>git-commit-id-plugin</artifactId>
   *       <executions>
   *           <execution>
   *               <id>get-the-git-infos</id>
   *               <goals>
   *                   <goal>revision</goal>
   *               </goals>
   *           </execution>
   *           <execution>
   *               <id>validate-the-git-infos</id>
   *               <goals>
   *                   <goal>validateRevision</goal>
   *               </goals>
   *               <phase>package</phase>
   *           </execution>
   *       </executions>
   *       <configuration>
   *           <dotGitDirectory>${project.basedir}/.git</dotGitDirectory>
   *           <prefix>git</prefix>
   *           <verbose>false</verbose>
   *           <generateGitPropertiesFile>true</generateGitPropertiesFile>
   *           <generateGitPropertiesFilename>
   *               ${project.build.outputDirectory}/git.properties
   *           </generateGitPropertiesFilename>
   *           <format>json</format>
   *           <gitDescribe>
   *               <skip>false</skip>
   *               <always>false</always>
   *               <dirty>-dirty</dirty>
   *           </gitDescribe>
   *           <!--
   *           <validationProperties>
   *               <validationProperty>
   *                   <name>validating git dirty</name>
   *                   <value>${git.dirty}</value>
   *                   <shouldMatchTo>false</shouldMatchTo>
   *               </validationProperty>
   *           </validationProperties>
   *           -->
   *       </configuration>
   *   </plugin>
   * </pre>
   *
   * @return properties.
   */
  private Map<String, Object> loadGitProperties(String properties) {

    Preconditions.checkNotNull(properties, "properties should not be null");

    try (InputStream is = TaskLogFormatter.class.getClassLoader().getResourceAsStream(properties)) {

      if (is == null) {
        return new HashMap<>();
      }

      StringBuilder builder = new StringBuilder();

      try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

        @Var
        String line;

        while ((line = br.readLine()) != null) {
          builder.append(line).append('\n');
        }
      }
      return asObject(builder.toString());
    } catch (IOException e) {
      return new HashMap<>();
    }
  }

  enum eLogLevel {
    TRACE, DEBUG, INFO, WARN, ERROR, FATAL
  }

  private enum ScanState {
    NEXT, KEY, VAL
  }
}
