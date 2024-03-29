package io.github.comrada.crypto.wtc.repository;

import io.github.comrada.crypto.wtc.model.WhaleAlert;
import io.github.comrada.crypto.wtc.model.WhaleAlert.ProcessingStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface WhaleAlertRepository extends JpaRepository<WhaleAlert, Long> {

  @Query("select wa from WhaleAlert wa where wa.processStatus = :status and wa.link is not null order by wa.id")
  List<WhaleAlert> findByStatus(ProcessingStatus status, Pageable pageable);

  @Transactional
  @Modifying(flushAutomatically = true)
  @Query("""
          update WhaleAlert wa set wa.processedAt = :processedAt, wa.processStatus = :status
          where wa.id = :id
      """)
  void setStatus(long id, ProcessingStatus status, Instant processedAt);

  @Query("select w from WhaleAlert w where w.id = :id and w.processStatus = :status")
  Optional<WhaleAlert> findByIdAndStatus(long id, ProcessingStatus status);

  default List<WhaleAlert> findNewAlerts(int limit) {
    return findByStatus(ProcessingStatus.NEW, PageRequest.of(0, limit));
  }

  default List<WhaleAlert> findFailedAlerts(int limit) {
    return findByStatus(ProcessingStatus.FAILED, PageRequest.of(0, limit));
  }

  default List<WhaleAlert> findProcessedAlerts(int limit) {
    return findByStatus(ProcessingStatus.DONE, PageRequest.of(0, limit));
  }

  default void inProgress(long id, Instant processedAt) {
    setStatus(id, ProcessingStatus.IN_PROGRESS, processedAt);
  }

  default void done(long id, Instant processedAt) {
    setStatus(id, ProcessingStatus.DONE, processedAt);
  }

  default void fail(long id, Instant processedAt) {
    setStatus(id, ProcessingStatus.FAILED, processedAt);
  }

  @Transactional(isolation = Isolation.SERIALIZABLE)
  default Optional<WhaleAlert> selectForExecution() {
    Optional<WhaleAlert> first = findNewAlerts(1).stream().findFirst();
    first.ifPresent(alert -> inProgress(alert.getId(), Instant.now()));
    return first;
  }

  @Transactional(isolation = Isolation.SERIALIZABLE)
  default Optional<WhaleAlert> selectForExecution(long id) {
    Optional<WhaleAlert> foundAlert = findByIdAndStatus(id, ProcessingStatus.NEW);
    if (foundAlert.isPresent()) {
      inProgress(id, Instant.now());
    }
    return foundAlert;
  }
}
