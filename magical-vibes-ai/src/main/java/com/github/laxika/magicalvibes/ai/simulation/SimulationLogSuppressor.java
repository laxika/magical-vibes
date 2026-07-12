package com.github.laxika.magicalvibes.ai.simulation;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;

/**
 * Suppresses game-engine INFO/DEBUG/TRACE logging on threads that are running MCTS
 * simulations. Every rollout action goes through the same {@code GameService} code paths
 * as the live game, so an unfiltered search formats and writes megabytes of engine log
 * output per decision — and the console appender's lock partially serializes the parallel
 * search workers. As a logback {@link TurboFilter} this runs before message formatting,
 * so suppressed events cost only an MDC lookup.
 * <p>
 * Scoped deliberately narrowly: only loggers under the engine's service package are
 * denied, and only below ERROR, and only while the {@link #MDC_KEY} flag is set on the
 * current thread (see {@link #enterSimulation()}). Live-game logging on other threads and
 * the AI module's own diagnostics (e.g. {@code MCTSEngine} debug output) are unaffected.
 * Engine WARNs are suppressed too: rollouts replay tree paths onto freshly determinized
 * states, so intentionally-attempted-but-illegal actions (e.g. "card not playable") are
 * routine there — the engine's swallowed-failure counters, not logs, track them.
 * <p>
 * Installed programmatically via {@link #install()} rather than through a logback config
 * file, so it is active in production, tests, and benchmarks alike without requiring
 * each classpath to ship its own logging configuration.
 */
public final class SimulationLogSuppressor extends TurboFilter {

    /** MDC flag marking the current thread as running inside an MCTS simulation. */
    public static final String MDC_KEY = "mctsSimulation";

    private static final String ENGINE_LOGGER_PREFIX = "com.github.laxika.magicalvibes.service";

    /**
     * Registers the filter with the current logback context if it is not already
     * present. Idempotence is checked against the context's live filter list (not a
     * static flag) so a logging-system restart that clears the filters gets the
     * suppressor re-installed by the next engine construction. No-op when logback is
     * not the bound SLF4J backend.
     */
    public static void install() {
        if (!(LoggerFactory.getILoggerFactory() instanceof LoggerContext context)) {
            return;
        }
        synchronized (SimulationLogSuppressor.class) {
            boolean present = context.getTurboFilterList().stream()
                    .anyMatch(filter -> filter instanceof SimulationLogSuppressor);
            if (!present) {
                context.addTurboFilter(new SimulationLogSuppressor());
            }
        }
    }

    /** Marks the current thread as simulating. Must be paired with {@link #exitSimulation()} in a finally block. */
    public static void enterSimulation() {
        MDC.put(MDC_KEY, "1");
    }

    /** Clears the simulation flag from the current thread. */
    public static void exitSimulation() {
        MDC.remove(MDC_KEY);
    }

    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t) {
        if (level.isGreaterOrEqual(Level.ERROR)) {
            return FilterReply.NEUTRAL;
        }
        if (MDC.get(MDC_KEY) == null) {
            return FilterReply.NEUTRAL;
        }
        return logger.getName().startsWith(ENGINE_LOGGER_PREFIX)
                ? FilterReply.DENY
                : FilterReply.NEUTRAL;
    }
}
