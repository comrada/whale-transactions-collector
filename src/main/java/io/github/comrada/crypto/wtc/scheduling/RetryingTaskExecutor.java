package io.github.comrada.crypto.wtc.scheduling;

import static java.util.Objects.requireNonNull;

import io.github.comrada.crypto.wtc.exception.Retryable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetryingTaskExecutor<T> implements TaskExecutor<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(RetryingTaskExecutor.class);
  private static final int RETRY_DELAY_MSEC = 10000;
  private final TaskExecutor<T> origin;
  private final int maxAttempts;


  public RetryingTaskExecutor(TaskExecutor<T> origin, int maxAttempts) {
    this.origin = requireNonNull(origin);
    this.maxAttempts = maxAttempts;
  }

  @Override
  public void submit(T task) {
    try {
      execute(task, 0);
    } catch (InterruptedException e) {
      throw new ExecutionException(e);
    }
  }

  private void execute(T task, int attempt) throws InterruptedException {
    try {
      origin.submit(task);
    } catch (Throwable t) {
      if (stopBecauseOf(t)) {
        LOGGER.error("An exception has occurred that does not involve a retry.", t);
        throw t;
      }
      if (++attempt > maxAttempts) {
        LOGGER.error("Exceeded the number of attempts to execute the task", t);
        throw t;
      }
      LOGGER.warn("An error occurred while running the task, attempt {} of {}", attempt, maxAttempts);
      Thread.sleep(RETRY_DELAY_MSEC);
      execute(task, attempt);
    }
  }

  private boolean stopBecauseOf(Throwable t) {
    return (t instanceof Retryable retryableException && !retryableException.retry()) ||
        (t.getCause() instanceof Retryable retryableCause && !retryableCause.retry());
  }
}
