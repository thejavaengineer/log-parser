package org.thejavaengineer.logparser;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ParsedLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String timestamp;
    private String sourceIp;
    private String method;
    private String path;
    private Integer statusCode;
    private Long size;
    private String logFormat;
    private String rawLog;
    private String sourceFile;

    @Column(columnDefinition = "TEXT")
    private String additionalFields; // JSON string for extra fields like user, referrer, etc.
}