package com.github.laxika.magicalvibes.testutil;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Fails a {@link BaseCardTest} (on a test method, or on every test of the class) whose game reaches a legacy
 * departure fallback, reported by
 * {@link com.github.laxika.magicalvibes.service.battlefield.LegacyDepartureFallback}: a removal that could no longer
 * ask its replacement question before the permanent left the battlefield.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface FailOnLegacyDepartureFallback {
}
