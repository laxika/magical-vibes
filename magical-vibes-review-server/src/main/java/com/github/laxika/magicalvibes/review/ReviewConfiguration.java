package com.github.laxika.magicalvibes.review;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Keeps the review database independent of the game's database and component graph. */
@Configuration
public class ReviewConfiguration {
    @Bean
    Path reviewRepositoryRoot(@Value("${review.repository-root:}") String configured) {
        Path candidate = Path.of(configured.isBlank() ? System.getProperty("user.dir") : configured)
                .toAbsolutePath().normalize();
        if (!configured.isBlank()) {
            return candidate;
        }
        while (candidate != null && !Files.exists(candidate.resolve("settings.gradle.kts"))) {
            candidate = candidate.getParent();
        }
        if (candidate == null) {
            throw new IllegalStateException("Set REVIEW_REPOSITORY_ROOT to a magical-vibes checkout");
        }
        return candidate;
    }

    @Bean(destroyMethod = "close")
    DataSource reviewDataSource(Path reviewRepositoryRoot,
                               @Value("${review.database-path:magical-vibes-review-server/review.sqlite}") String database)
            throws IOException {
        Path path = reviewRepositoryRoot.resolve(database).normalize();
        Files.createDirectories(path.getParent());
        HikariDataSource source = new HikariDataSource();
        source.setJdbcUrl("jdbc:sqlite:" + path);
        source.setDriverClassName("org.sqlite.JDBC");
        source.setMaximumPoolSize(1);
        source.setMinimumIdle(1);
        source.addDataSourceProperty("foreign_keys", "true");
        source.addDataSourceProperty("busy_timeout", "5000");
        source.addDataSourceProperty("journal_mode", "DELETE");
        return source;
    }
}
