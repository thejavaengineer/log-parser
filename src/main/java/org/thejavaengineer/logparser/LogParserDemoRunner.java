package org.thejavaengineer.logparser;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LogParserDemoRunner implements CommandLineRunner {

    private final LogParser logParser;

    @Override
    public void run(String... args) {
        System.out.println("Log Parser Demo");
        System.out.println("=".repeat(50));

        String[] sampleLogs = {
                "192.168.1.20 - - [28/Jul/2023:10:27:10 +0000] \"GET /index.html HTTP/1.1\" 200 2326",
                "192.168.1.10 - john [28/Jul/2023:10:27:10 +0000] \"GET /api/users HTTP/1.1\" 404 52 \"https://example.com\" \"Mozilla/5.0\"",
                "{\"timestamp\": \"2023-07-28T10:27:10Z\", \"level\": \"ERROR\", \"message\": \"Database connection failed\", \"service\": \"user-api\"}",
                "<34>1 2023-07-28T10:27:10Z myhost myapp 1234 ID47 [example@1234 foo=\"bar\"] This is a syslog message" // Syslog example
        };

        for (int i = 0; i < sampleLogs.length; i++) {
            String log = sampleLogs[i];
            System.out.println("\nLog #" + (i + 1) + ":");
            System.out.println("Raw: " + (log.length() > 60 ? log.substring(0, 60) + "..." : log));
            String format = logParser.detectFormat(log);
            System.out.println("Detected Format: " + format);
            ParsedLog parsed = logParser.parse(log, "demo");
            System.out.println("Parsed Result: " + parsed);
            System.out.println("-".repeat(50));
        }
    }
}
