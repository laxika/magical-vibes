package com.github.laxika.magicalvibes.testutil;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;

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
                    Map.of("oracle.data-load-mode", "ON_DEMAND")));
            created.register(GameTestDoublesConfig.class);
            created.refresh();
            context = created;
            return created;
        }
    }
}
