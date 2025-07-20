package org.thejavaengineer.logparser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for parsing log lines.
 * It can detect and parse different log formats like Apache, Nginx, JSON, and Syslog.
 */
@Service
public class LogParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Detects the format of a log line.
     *
     * @param logLine The log line to analyze.
     * @return The detected format (e.g., "json", "apache", "nginx", "syslog", or "unknown").
     */
    public String detectFormat(String logLine) {
        // JSON check
        try {
            objectMapper.readTree(logLine);
            return "json";
        } catch (JsonProcessingException ignored) {
        }

        // Apache pattern
        Pattern apachePattern = Pattern.compile("^\\S+ - - \\[\\d+/\\w+/\\d+:\\d+:\\d+:\\d+ [+-]\\d+\\]");
        if (apachePattern.matcher(logLine).find()) {
            return "apache";
        }

        // Nginx pattern
        Pattern nginxPattern = Pattern.compile("^\\S+ - \\S+ \\[\\d+/\\w+/\\d+:\\d+:\\d+:\\d+ [+-]\\d+\\]");
        if (nginxPattern.matcher(logLine).find()) {
            return "nginx";
        }

        // Syslog pattern (added for assignment)
        Pattern syslogPattern = Pattern.compile("^<\\d+>\\d+ \\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*");
        if (syslogPattern.matcher(logLine).find()) {
            return "syslog";
        }

        return "unknown";
    }

    /**
     * Parses a log line into a ParsedLog object.
     *
     * @param logLine    The log line to parse.
     * @param sourceFile The file from which the log line was read.
     * @return A ParsedLog object containing the parsed data.
     */
    public ParsedLog parse(String logLine, String sourceFile) {
        ParsedLog parsedLog = new ParsedLog();
        parsedLog.setRawLog(logLine);
        parsedLog.setSourceFile(sourceFile);
        String format = detectFormat(logLine);
        parsedLog.setLogFormat(format);

        try {
            switch (format) {
                case "apache" -> parseApache(logLine, parsedLog);
                case "nginx" -> parseNginx(logLine, parsedLog);
                case "json" -> parseJson(logLine, parsedLog);
                case "syslog" -> parseSyslog(logLine, parsedLog);
                default -> {} // Already set to unknown
            }
        } catch (Exception e) {
            parsedLog.setLogFormat("unknown"); // Error handling
        }

        return parsedLog;
    }

    /**
     * Parses an Apache log line.
     *
     * @param logLine   The log line to parse.
     * @param parsedLog The ParsedLog object to populate.
     */
    private void parseApache(String logLine, ParsedLog parsedLog) {
        Pattern pattern = Pattern.compile("(\\S+) - - \\[(\\d+/\\w+/\\d+):(\\d+:\\d+:\\d+) ([+-]\\d+)\\] \"(\\S+) (\\S+) ([^\"]+)\" (\\d+) (\\d+|-)");
        Matcher matcher = pattern.matcher(logLine);
        if (matcher.matches()) {
            parsedLog.setSourceIp(matcher.group(1));
            String date = matcher.group(2);
            String time = matcher.group(3);
            String tz = matcher.group(4);
            parsedLog.setTimestamp(convertToIso(date, time, tz));
            parsedLog.setMethod(matcher.group(5));
            parsedLog.setPath(matcher.group(6));
            parsedLog.setStatusCode(Integer.parseInt(matcher.group(8)));
            parsedLog.setSize(matcher.group(9).equals("-") ? 0L : Long.parseLong(matcher.group(9)));
        }
    }

    /**
     * Parses an Nginx log line.
     *
     * @param logLine   The log line to parse.
     * @param parsedLog The ParsedLog object to populate.
     */
    private void parseNginx(String logLine, ParsedLog parsedLog) {
        Pattern pattern = Pattern.compile("(\\S+) - (\\S+) \\[(\\d+/\\w+/\\d+):(\\d+:\\d+:\\d+) ([+-]\\d+)\\] \"(\\S+) (\\S+) ([^\"]+)\" (\\d+) (\\d+) \"([^\"]*)\" \"([^\"]*)\"");
        Matcher matcher = pattern.matcher(logLine);
        if (matcher.matches()) {
            parsedLog.setSourceIp(matcher.group(1));
            String user = matcher.group(2).equals("-") ? null : matcher.group(2);
            String date = matcher.group(3);
            String time = matcher.group(4);
            String tz = matcher.group(5);
            parsedLog.setTimestamp(convertToIso(date, time, tz));
            parsedLog.setMethod(matcher.group(6));
            parsedLog.setPath(matcher.group(7));
            parsedLog.setStatusCode(Integer.parseInt(matcher.group(9)));
            parsedLog.setSize(Long.parseLong(matcher.group(10)));
            Map<String, Object> extras = new HashMap<>();
            extras.put("user", user);
            extras.put("referrer", matcher.group(11).equals("-") ? null : matcher.group(11));
            extras.put("userAgent", matcher.group(12));
            parsedLog.setAdditionalFields(toJson(extras));
        }
    }

    /**
     * Parses a JSON log line.
     *
     * @param logLine   The log line to parse.
     * @param parsedLog The ParsedLog object to populate.
     * @throws JsonProcessingException If there is an error parsing the JSON.
     */
    private void parseJson(String logLine, ParsedLog parsedLog) throws JsonProcessingException {
        JsonNode json = objectMapper.readTree(logLine);
        parsedLog.setTimestamp(json.path("timestamp").asText());
        parsedLog.setSourceIp(json.path("ip").asText(json.path("source_ip").asText(null))); // Map common fields
        Map<String, Object> extras = new HashMap<>();
        json.fields().forEachRemaining(entry -> extras.put(entry.getKey(), entry.getValue().asText()));
        parsedLog.setAdditionalFields(toJson(extras));
    }

    /**
     * Parses a Syslog log line.
     *
     * @param logLine   The log line to parse.
     * @param parsedLog The ParsedLog object to populate.
     */
    private void parseSyslog(String logLine, ParsedLog parsedLog) {
        // Basic Syslog parser: <priority>version timestamp hostname app-name procid msgid [structured-data] message
        Pattern pattern = Pattern.compile("^<(\\d+)>\\d+ (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[^ ]*) (\\S+) (\\S+) (\\S+) (\\S+) \\[([^]]*)\\] (.*)");
        Matcher matcher = pattern.matcher(logLine);
        if (matcher.matches()) {
            parsedLog.setTimestamp(matcher.group(2));
            parsedLog.setSourceIp(matcher.group(3)); // Hostname as proxy for IP
            Map<String, Object> extras = new HashMap<>();
            extras.put("priority", matcher.group(1));
            extras.put("appName", matcher.group(4));
            extras.put("procId", matcher.group(5));
            extras.put("msgId", matcher.group(6));
            extras.put("structuredData", matcher.group(7));
            extras.put("message", matcher.group(8));
            parsedLog.setAdditionalFields(toJson(extras));
        }
    }

    /**
     * Converts a date, time, and timezone into an ISO 8601 formatted string.
     *
     * @param date The date string.
     * @param time The time string.
     * @param tz   The timezone string.
     * @return The ISO 8601 formatted date-time string.
     */
    private String convertToIso(String date, String time, String tz) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z");
            ZonedDateTime dt = LocalDateTime.parse(date + ":" + time, DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss")).atZone(ZoneId.of(tz));
            return dt.format(DateTimeFormatter.ISO_DATE_TIME);
        } catch (DateTimeParseException e) {
            return date + "T" + time + tz;
        }
    }

    /**
     * Converts a map to a JSON string.
     *
     * @param map The map to convert.
     * @return The JSON string representation of the map.
     */
    private String toJson(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
