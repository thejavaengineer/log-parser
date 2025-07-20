package org.thejavaengineer.logparser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Log Parser.
 */
@SpringBootApplication
public class LogParserApplication {

    /**
     * Main method to run the Spring Boot application.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        SpringApplication.run(LogParserApplication.class, args);
    }

}
