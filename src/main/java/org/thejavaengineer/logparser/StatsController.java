package org.thejavaengineer.logparser;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller for exposing log statistics.
 */
@RestController
@RequiredArgsConstructor
public class StatsController {

    private final LogStatsService statsService;

    /**
     * Returns a map of the current statistics.
     *
     * @return A map of the current statistics.
     */
    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        return statsService.getStats();
    }
}
