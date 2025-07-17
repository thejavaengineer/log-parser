package org.thejavaengineer.logparser;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ParsedLogRepository extends JpaRepository<ParsedLog, Long> {
}