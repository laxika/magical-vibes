package com.github.laxika.magicalvibes.testutil;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Cached Spring test context shared by every card test via {@link GameTestHarness}.
 * One JVM fork → one context → each requested oracle set and the effect registry loaded once.
 */
public final class GameTestEngineContext {

    private static volatile AnnotationConfigApplicationContext context;

    private GameTestEngineContext() {
    }

    public static AnnotationConfigApplicationContext get() {
        AnnotationConfigApplicationContext existing = context;
        if (existing != null) {
            return existing;
        }
        synchronized (GameTestEngineContext.class) {
            existing = context;
            if (existing != null) {
                return existing;
            }
            AnnotationConfigApplicationContext created = new AnnotationConfigApplicationContext();
            created.getEnvironment().getPropertySources().addFirst(new MapPropertySource(
                    "game-test-oracle-loading",
                    Map.of(
                            "oracle.data-load-mode", "ON_DEMAND",
                            // Module working directories differ (application vs ai); use the shared
                            // repo-root cache so on-demand loads do not re-fetch every set per module.
                            "card-data.cache-dir", locateRepoRoot().resolve("card-data-cache").toString())));
            created.register(GameTestDoublesConfig.class);
            created.refresh();
            context = created;
            return created;
        }
    }

    /** Walk up from the test working directory until a Gradle settings file is found. */
    private static Path locateRepoRoot() {
        Path dir = Path.of("").toAbsolutePath();
        for (Path p = dir; p != null; p = p.getParent()) {
            if (Files.exists(p.resolve("settings.gradle.kts")) || Files.exists(p.resolve("settings.gradle"))) {
                return p;
            }
        }
        throw new IllegalStateException(
                "Could not locate repo root (no settings.gradle[.kts]) walking up from " + dir);
    }
}
