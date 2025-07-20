package org.thejavaengineer.logparser;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link ParsedLog} entities.
 */
public interface ParsedLogRepository extends JpaRepository<ParsedLog, Long> {
}
