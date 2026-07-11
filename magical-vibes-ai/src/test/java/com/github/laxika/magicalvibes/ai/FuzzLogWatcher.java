package com.github.laxika.magicalvibes.ai;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.AppenderBase;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Logback appender that turns swallowed engine/AI failures into fuzz-test failures.
 *
 * <p>Every exception thrown while an AI drives the engine is caught somewhere in the
 * loop ({@code AiDecisionEngine.send()}, {@code AiConnection}, the blocker fallbacks)
 * and only logged — without this watcher a crash degrades into a "stuck game" report
 * or is masked entirely when the AI recovers by passing priority. The watcher records:
 * <ul>
 *   <li>every ERROR event from project loggers with its stack trace (the engine's main
 *       sources never log at ERROR level in normal operation, so these are always
 *       caught crashes), and</li>
 *   <li>WARN events marking AI/engine legality disagreements — the AI computed an
 *       action as legal but the engine rejected it, which is either an AI bug or an
 *       engine validation bug.</li>
 * </ul>
 */
final class FuzzLogWatcher extends AppenderBase<ILoggingEvent> {

    private static final String PROJECT_LOGGER_PREFIX = "com.github.laxika.magicalvibes";
    private static final List<String> LEGALITY_DISAGREEMENT_MARKERS = List.of(
            "PlayCard failed silently",
            "ActivateAbility failed silently",
            "Blocker declaration threw",
            "Blocker declaration rejected");

    private final ConcurrentLinkedQueue<String> failures = new ConcurrentLinkedQueue<>();

    /** Creates a watcher and attaches it to the root logger. */
    static FuzzLogWatcher install() {
        FuzzLogWatcher watcher = new FuzzLogWatcher();
        watcher.setName("fuzz-log-watcher");
        Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        watcher.setContext(root.getLoggerContext());
        watcher.start();
        root.addAppender(watcher);
        return watcher;
    }

    void uninstall() {
        Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.detachAppender(this);
        stop();
    }

    /** Returns and clears all failures recorded so far. */
    List<String> drainFailures() {
        List<String> drained = new ArrayList<>();
        String failure;
        while ((failure = failures.poll()) != null) {
            drained.add(failure);
        }
        return drained;
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (!event.getLoggerName().startsWith(PROJECT_LOGGER_PREFIX)) {
            return;
        }
        String message = event.getFormattedMessage();
        boolean error = event.getLevel().isGreaterOrEqual(Level.ERROR);
        boolean legalityDisagreement = event.getLevel() == Level.WARN
                && LEGALITY_DISAGREEMENT_MARKERS.stream().anyMatch(message::contains);
        if (!error && !legalityDisagreement) {
            return;
        }
        StringBuilder sb = new StringBuilder()
                .append(error ? "[ERROR] " : "[LEGALITY] ")
                .append(event.getLoggerName()).append(": ").append(message);
        IThrowableProxy throwableProxy = event.getThrowableProxy();
        if (throwableProxy != null) {
            sb.append('\n').append(ThrowableProxyUtil.asString(throwableProxy));
        }
        failures.add(sb.toString());
    }
}
