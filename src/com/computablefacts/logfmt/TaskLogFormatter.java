package com.computablefacts.logfmt;

import java.util.UUID;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;

@CheckReturnValue
public class TaskLogFormatter extends LogFormatter {

  protected final Task task_; // infos about the task
  protected final Environment env_; // infos about the environment running the task
  protected final User user_; // infos about the caller/user who triggered the task

  protected TaskLogFormatter(Task task, Environment env) {
    this(task, env, new User());
  }

  protected TaskLogFormatter(Task task, Environment env, User user) {
    task_ = Preconditions.checkNotNull(task, "task should not be null");
    env_ = Preconditions.checkNotNull(env, "env should not be null");
    user_ = Preconditions.checkNotNull(user, "user should not be null");
  }

  protected TaskLogFormatter(TaskLogFormatter task) {

    Preconditions.checkNotNull(task, "task should not be null");

    task_ = task.task_;
    env_ = task.env_;
    user_ = task.user_;
  }

  public static TaskLogFormatter create(TaskLogFormatter task) {
    return new TaskLogFormatter(task);
  }

  public static TaskLogFormatter create(Task task, Environment env) {
    return new TaskLogFormatter(task, env);
  }

  public static TaskLogFormatter create(Task task, Environment env, User user) {
    return new TaskLogFormatter(task, env, user);
  }

  public static String nextTaskId() {
    return UUID.randomUUID().toString();
  }

  @Override
  public synchronized String format() {

    env_.fill(this);
    task_.fill(this);
    user_.fill(this);

    return super.format();
  }

  public enum eEnv {
    UNKNOWN, LOCAL, DEV, STAGING, PROD
  }

  public static class Task {

    private final String taskName_;
    private final String taskId_;

    public Task(String taskName) {
      this(taskName, nextTaskId());
    }

    public Task(String taskName, String taskId) {
      taskName_ = Preconditions.checkNotNull(taskName, "taskName should not be null");
      taskId_ = Preconditions.checkNotNull(taskId, "taskId should not be null");
    }

    @CanIgnoreReturnValue
    public LogFormatter fill(LogFormatter formatter) {
      return formatter.add("task_name", taskName_).add("task_id", taskId_);
    }
  }

  public static class Environment {

    private final eEnv env_;

    public Environment(eEnv env) {
      env_ = Preconditions.checkNotNull(env, "env should not be null");
    }

    @CanIgnoreReturnValue
    public LogFormatter fill(LogFormatter logger) {
      return logger.add("env", env_.toString());
    }
  }

  public static class User {

    private final String clientId_;
    private final String clientName_;
    private final String userId_;
    private final String userName_;
    private final String userEmail_;

    public User() {
      this("", "", "", "", "");
    }

    public User(String clientId, String clientName, String userId, String userName,
        String userEmail) {
      clientId_ = Preconditions.checkNotNull(clientId, "clientId should not be null");
      clientName_ = Preconditions.checkNotNull(clientName, "clientName should not be null");
      userId_ = Preconditions.checkNotNull(userId, "userId should not be null");
      userName_ = Preconditions.checkNotNull(userName, "userName should not be null");
      userEmail_ = Preconditions.checkNotNull(userEmail, "userEmail should not be null");
    }

    @CanIgnoreReturnValue
    public LogFormatter fill(LogFormatter formatter) {
      return formatter.add("client_id", clientId_).add("client_name", clientName_)
          .add("user_id", userId_).add("user_name", userName_).add("user_email", userEmail_);
    }
  }
}
