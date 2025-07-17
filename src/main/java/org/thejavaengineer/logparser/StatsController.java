package org.thejavaengineer.logparser;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class StatsController {

    private final LogStatsService statsService;

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        return statsService.getStats();
    }
}