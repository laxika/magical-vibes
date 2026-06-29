package com.github.laxika.magicalvibes.ai;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Spring wiring for the AI module: component-scans the {@code ai} package so the application
 * context picks up {@link AiPlayerService} and the supporting beans. Imported by the backend
 * application instead of having the backend reach in with its own component scan.
 *
 * <p>The headless-simulation doubles config under {@code ai.simulation} is deliberately not a
 * {@code @Configuration} stereotype, so this scan does not register it in the main context — it
 * is only ever loaded explicitly by the headless simulation harness.
 */
@Configuration
@ComponentScan(basePackages = "com.github.laxika.magicalvibes.ai")
public class AiConfig {
}
