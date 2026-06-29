package com.github.laxika.magicalvibes.testutil;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Cached Spring test context shared by every card test via {@link GameTestHarness}.
 * One JVM fork → one context → one Scryfall load and one effect registration pass.
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
            AnnotationConfigApplicationContext created = new AnnotationConfigApplicationContext(GameTestDoublesConfig.class);
            context = created;
            return created;
        }
    }
}
