package org.thejavaengineer.logparser;


import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for tracking statistics about the parsed logs.
 */
@Service
public class LogStatsService {

    private final Map<Integer, AtomicInteger> statusCodeCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> sourceIpCounts = new ConcurrentHashMap<>();
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicInteger failureCount = new AtomicInteger(0);

    /**
     * Updates the statistics with the data from a parsed log.
     *
     * @param parsed The parsed log.
     */
    public void updateStats(ParsedLog parsed) {
        if (!"unknown".equals(parsed.getLogFormat())) {
            successCount.incrementAndGet();
        } else {
            failureCount.incrementAndGet();
        }
        if (parsed.getStatusCode() != null) {
            statusCodeCounts.computeIfAbsent(parsed.getStatusCode(), k -> new AtomicInteger(0)).incrementAndGet();
        }
        if (parsed.getSourceIp() != null) {
            sourceIpCounts.computeIfAbsent(parsed.getSourceIp(), k -> new AtomicInteger(0)).incrementAndGet();
        }
    }

    /**
     * Returns a map of the current statistics.
     *
     * @return A map of the current statistics.
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("successCount", successCount.get());
        stats.put("failureCount", failureCount.get());
        stats.put("statusCodeCounts", statusCodeCounts);
        stats.put("sourceIpCounts", sourceIpCounts);
        return stats;
    }
}
