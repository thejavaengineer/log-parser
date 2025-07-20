package org.thejavaengineer.logparser;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Represents a parsed log entry.
 */
@Entity
@Data
public class ParsedLog {
    /**
     * The unique ID of the log entry.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The timestamp of the log entry.
     */
    private String timestamp;
    /**
     * The source IP address of the log entry.
     */
    private String sourceIp;
    /**
     * The HTTP method of the log entry.
     */
    private String method;
    /**
     * The path of the log entry.
     */
    private String path;
    /**
     * The status code of the log entry.
     */
    private Integer statusCode;
    /**
     * The size of the response in bytes.
     */
    private Long size;
    /**
     * The format of the log entry.
     */
    private String logFormat;
    /**
     * The raw log line.
     */
    private String rawLog;
    /**
     * The source file of the log entry.
     */
    private String sourceFile;

    /**
     * Additional fields from the log entry, stored as a JSON string.
     */
    @Column(columnDefinition = "TEXT")
    private String additionalFields; // JSON string for extra fields like user, referrer, etc.
}
