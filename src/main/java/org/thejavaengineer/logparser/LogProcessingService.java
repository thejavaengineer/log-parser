package org.thejavaengineer.logparser;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * Service for processing log files.
 * It watches a directory for new log files and processes them as they appear.
 */
@Service
@RequiredArgsConstructor
public class LogProcessingService {

    private final LogParser logParser;
    private final ParsedLogRepository repository;
    private final LogStatsService statsService; // Injected for stats

    private final Set<Integer> processedLines = new HashSet<>(); // HashSet for line hashes
    private static final String LOG_DIR = System.getenv("LOG_DIR") != null ? System.getenv("LOG_DIR") : "./logs";

    /**
     * Starts watching the log directory for changes.
     *
     * @throws IOException If an I/O error occurs.
     */
    @PostConstruct
    public void startWatching() throws IOException {
        System.out.println("Starting log processing service. Monitoring directory: " + LOG_DIR);
        Path dir = Paths.get(LOG_DIR);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }

        // Process existing files
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.log")) {
            for (Path file : stream) {
                processFile(file.toString());
            }
        }

        // Start watcher
        WatchService watchService = FileSystems.getDefault().newWatchService();
        dir.register(watchService, ENTRY_MODIFY);

        new Thread(() -> {
            while (true) {
                try {
                    WatchKey key = watchService.take();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.kind() == ENTRY_MODIFY) {
                            Path filePath = dir.resolve((Path) event.context());
                            if (filePath.toString().endsWith(".log")) {
                                processFile(filePath.toString());
                            }
                        }
                    }
                    key.reset();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    /**
     * Processes a single log file.
     *
     * @param filePath The path to the log file.
     */
    private void processFile(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int lineHash = line.hashCode();
                if (!processedLines.contains(lineHash)) {
                    ParsedLog parsed = logParser.parse(line.trim(), filePath);
                    repository.save(parsed);
                    statsService.updateStats(parsed); // Update stats
                    processedLines.add(lineHash);
                    if (processedLines.size() > 100000) {
                        // Trim to last 50000
                        processedLines.clear(); // Simple reset for demo; use LRU in prod
                    }
                    System.out.println("Parsed log from " + filePath + ": " + parsed.toString().substring(0, Math.min(100, parsed.toString().length())) + "...");
                }
            }
        } catch (IOException e) {
            System.err.println("Error processing file " + filePath + ": " + e.getMessage());
        }
    }
}
