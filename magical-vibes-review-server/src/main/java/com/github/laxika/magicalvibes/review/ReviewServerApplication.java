package com.github.laxika.magicalvibes.review;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Standalone composition root for review coordination and its dashboard. */
@SpringBootApplication
public class ReviewServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReviewServerApplication.class, args);
    }
}
