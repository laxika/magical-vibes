package com.github.laxika.magicalvibes.ai;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.AppenderBase;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Logback appender that turns swallowed engine/AI failures into fuzz-test failures.
 *
 * <p>Every exception thrown while an AI drives the engine is caught somewhere in the
 * loop ({@code AiDecisionEngine.send()}, {@code AiDecisionScheduler}, the blocker fallbacks)
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
 *
 * <p>A legality disagreement on its own rarely says <em>why</em>. The AI reports that a cast
 * failed silently and prints the pool it had, but the reason the pool was short is in the
 * rejections the engine issued moments earlier — and those are swallowed at INFO by
 * {@code AiGameActions}, whose whole job is to make an illegal action a no-op. So the recent
 * ones are kept in a rolling window and stapled onto whatever failure follows: a cast that
 * came up one mana short then reads "engine rejected tapPermanent … : Creature has summoning
 * sickness" instead of leaving the gate to be guessed at.
 */
final class FuzzLogWatcher extends AppenderBase<ILoggingEvent> {

    private static final String PROJECT_LOGGER_PREFIX = "com.github.laxika.magicalvibes";
    private static final List<String> LEGALITY_DISAGREEMENT_MARKERS = List.of(
            "PlayCard failed silently",
            "ActivateAbility failed silently",
            "Blocker declaration threw",
            "Blocker declaration rejected");
    /**
     * Engine refusals worth quoting back: every {@code AiGameActions} swallow site shares the
     * first marker, and {@code SpellCastingService} logs the second with the pool, the playable
     * indices and the hand it decided against.
     */
    private static final List<String> REJECTION_CONTEXT_MARKERS = List.of(
            "AI: engine rejected",
            "not playable");
    /** How many recent refusals to keep. A priority pass taps at most a board's worth of sources. */
    private static final int MAX_REJECTION_CONTEXT = 40;

    private final ConcurrentLinkedQueue<String> failures = new ConcurrentLinkedQueue<>();
    private final Deque<String> recentRejections = new ConcurrentLinkedDeque<>();

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
            recordRejectionContext(event, message);
            return;
        }
        StringBuilder sb = new StringBuilder()
                .append(error ? "[ERROR] " : "[LEGALITY] ")
                .append(event.getLoggerName()).append(": ").append(message);
        IThrowableProxy throwableProxy = event.getThrowableProxy();
        if (throwableProxy != null) {
            sb.append('\n').append(ThrowableProxyUtil.asString(throwableProxy));
        }
        appendRejectionContext(sb);
        failures.add(sb.toString());
    }

    /**
     * Keeps the refusal in the rolling window, tagged with its thread so the two AI players'
     * lines stay apart. Both players log into one window; which one owns a line is exactly what
     * the thread name says.
     */
    private void recordRejectionContext(ILoggingEvent event, String message) {
        if (REJECTION_CONTEXT_MARKERS.stream().noneMatch(message::contains)) {
            return;
        }
        recentRejections.addLast("[" + event.getThreadName() + "] " + message);
        while (recentRejections.size() > MAX_REJECTION_CONTEXT) {
            recentRejections.pollFirst();
        }
    }

    /**
     * Stapled onto a failure, oldest first, so the refusals read in the order the engine issued
     * them. The window is left intact: a second failure in the same game is almost always the
     * same story, and consuming the lines would leave whichever AI thread reported second with
     * no context at all.
     */
    private void appendRejectionContext(StringBuilder sb) {
        List<String> context = new ArrayList<>(recentRejections);
        if (context.isEmpty()) {
            return;
        }
        sb.append("\n  engine refusals leading up to this (oldest first):");
        for (String rejection : context) {
            sb.append("\n    ").append(rejection);
        }
    }
}
