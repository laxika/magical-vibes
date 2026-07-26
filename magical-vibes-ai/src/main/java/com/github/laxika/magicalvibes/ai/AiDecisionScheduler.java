package com.github.laxika.magicalvibes.ai;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class AiDecisionScheduler {

    private static final long DEFAULT_DECISION_DELAY_MS = 800;
    private static final long SLOW_DISPATCH_THRESHOLD_MS = 1_000;

    private final String schedulerId;
    private final AiDecisionEngine engine;
    private final ScheduledThreadPoolExecutor executor;
    private final long decisionDelayMs;
    private final AtomicBoolean open = new AtomicBoolean(true);
    private final Object gameStateLock = new Object();
    private final AtomicLong receivedEvents = new AtomicLong();
    private final AtomicLong ignoredEvents = new AtomicLong();
    private final AtomicLong coalescedGameStates = new AtomicLong();
    private final AtomicLong handledDecisions = new AtomicLong();
    private boolean gameStateTaskScheduled;
    private boolean gameStateDirty;
    private volatile AiDecisionKind activeDecisionKind;
    private volatile AiDecisionKind lastHandledDecisionKind;

    public AiDecisionScheduler(String schedulerId, AiDecisionEngine engine) {
        this(schedulerId, engine, DEFAULT_DECISION_DELAY_MS);
    }

    public AiDecisionScheduler(String schedulerId, AiDecisionEngine engine, long decisionDelayMs) {
        this.schedulerId = schedulerId;
        this.engine = engine;
        this.decisionDelayMs = decisionDelayMs;

        ScheduledThreadPoolExecutor pool = new ScheduledThreadPoolExecutor(1, r -> {
            Thread t = new Thread(r, "ai-" + schedulerId);
            t.setDaemon(true);
            return t;
        });
        pool.setRemoveOnCancelPolicy(true);
        this.executor = pool;
    }

    public String getId() {
        return schedulerId;
    }

    public boolean isOpen() {
        return open.get();
    }

    public void scheduleDecision(AiDecisionKind kind) {
        if (!open.get()) {
            return;
        }
        receivedEvents.incrementAndGet();
        try {
            if (kind == AiDecisionKind.GAME_STATE) {
                scheduleGameState();
            } else {
                scheduleDecisionTask(kind);
            }
        } catch (RejectedExecutionException e) {
            // close() may race with a final event batch. A closed AI deliberately drops it.
            if (open.get()) {
                log.error("AI executor rejected decision event", e);
            }
        }
    }

    /**
     * Coalesces repeated state invalidations while preserving one follow-up decision when a
     * newer state arrives during the current decision. The decision engine reads live
     * GameData, so retaining every state invalidation is both unnecessary and
     * actively harmful: bursts of invalidations otherwise delay the current priority action.
     */
    private void scheduleGameState() {
        synchronized (gameStateLock) {
            gameStateDirty = true;
            if (gameStateTaskScheduled) {
                coalescedGameStates.incrementAndGet();
                return;
            }
            gameStateTaskScheduled = true;
            scheduleGameStateTask();
        }
    }

    private void scheduleGameStateTask() {
        long scheduledAtNanos = System.nanoTime();
        executor.schedule(() -> {
            synchronized (gameStateLock) {
                gameStateDirty = false;
            }
            handleScheduledEvent(AiDecisionKind.GAME_STATE, scheduledAtNanos);

            synchronized (gameStateLock) {
                if (open.get() && gameStateDirty) {
                    scheduleGameStateTask();
                } else {
                    gameStateTaskScheduled = false;
                    gameStateDirty = false;
                }
            }
        }, decisionDelayMs, TimeUnit.MILLISECONDS);
    }

    private void scheduleDecisionTask(AiDecisionKind kind) {
        long scheduledAtNanos = System.nanoTime();
        executor.schedule(() -> handleScheduledEvent(kind, scheduledAtNanos),
                decisionDelayMs, TimeUnit.MILLISECONDS);
    }

    private void handleScheduledEvent(AiDecisionKind kind, long scheduledAtNanos) {
        if (!open.get()) {
            return;
        }

        long dispatchDelayMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - scheduledAtNanos)
                - decisionDelayMs;
        if (dispatchDelayMs >= SLOW_DISPATCH_THRESHOLD_MS) {
            log.warn("AI scheduler {} dispatch of {} waited {} ms beyond its decision delay; {}",
                    schedulerId, kind, dispatchDelayMs, diagnosticSummary());
        }

        activeDecisionKind = kind;
        try {
            engine.handleEvent(kind);
            handledDecisions.incrementAndGet();
            lastHandledDecisionKind = kind;
        } catch (Exception e) {
            log.error("AI decision error for decision kind {}", kind, e);
        } finally {
            activeDecisionKind = null;
        }
    }

    public void close() {
        open.set(false);
        // Use shutdown() rather than shutdownNow() to avoid setting the interrupt flag on the
        // calling thread. A final game-end fact may be delivered from the AI's own executor
        // thread, and interrupting it would corrupt subsequent subscriber work.
        // Already-queued tasks check open before invoking the decision engine.
        executor.shutdown();
    }

    public String diagnosticSummary() {
        synchronized (gameStateLock) {
            return "open=" + open.get()
                    + " queuedTasks=" + executor.getQueue().size()
                    + " active=" + activeDecisionKind
                    + " lastHandled=" + lastHandledDecisionKind
                    + " gameStateScheduled=" + gameStateTaskScheduled
                    + " gameStateDirty=" + gameStateDirty
                    + " received=" + receivedEvents.get()
                    + " handled=" + handledDecisions.get()
                    + " ignored=" + ignoredEvents.get()
                    + " coalescedGameStates=" + coalescedGameStates.get();
        }
    }
}
