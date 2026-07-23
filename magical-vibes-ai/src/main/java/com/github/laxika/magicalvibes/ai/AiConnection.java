package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.networking.Connection;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Set;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class AiConnection implements Connection {

    private static final long DEFAULT_DECISION_DELAY_MS = 800;
    private static final long SLOW_DISPATCH_THRESHOLD_MS = 1_000;
    private static final String GAME_STATE = "GAME_STATE";
    private static final Set<String> ACTIONABLE_MESSAGE_TYPES = Set.of(
            GAME_STATE,
            "MULLIGAN_RESOLVED",
            "SELECT_CARDS_TO_BOTTOM",
            "AVAILABLE_ATTACKERS",
            "AVAILABLE_BLOCKERS",
            "INTERACTION_PROMPT",
            "COMBAT_DAMAGE_ASSIGNMENT"
    );

    private final String connectionId;
    private final AiDecisionEngine engine;
    private final ObjectMapper objectMapper;
    private final ScheduledThreadPoolExecutor executor;
    private final long decisionDelayMs;
    private final AtomicBoolean open = new AtomicBoolean(true);
    private final Object gameStateLock = new Object();
    private final AtomicLong receivedMessages = new AtomicLong();
    private final AtomicLong ignoredMessages = new AtomicLong();
    private final AtomicLong coalescedGameStates = new AtomicLong();
    private final AtomicLong handledMessages = new AtomicLong();
    private boolean gameStateTaskScheduled;
    private boolean gameStateDirty;
    private volatile String activeMessageType;
    private volatile String lastHandledMessageType;

    public AiConnection(String connectionId, AiDecisionEngine engine, ObjectMapper objectMapper) {
        this(connectionId, engine, objectMapper, DEFAULT_DECISION_DELAY_MS);
    }

    public AiConnection(String connectionId, AiDecisionEngine engine, ObjectMapper objectMapper, long decisionDelayMs) {
        this.connectionId = connectionId;
        this.engine = engine;
        this.objectMapper = objectMapper;
        this.decisionDelayMs = decisionDelayMs;

        ScheduledThreadPoolExecutor pool = new ScheduledThreadPoolExecutor(1, r -> {
            Thread t = new Thread(r, "ai-" + connectionId);
            t.setDaemon(true);
            return t;
        });
        pool.setRemoveOnCancelPolicy(true);
        this.executor = pool;
    }

    @Override
    public String getId() {
        return connectionId;
    }

    @Override
    public boolean isOpen() {
        return open.get();
    }

    @Override
    public void sendMessage(String message) {
        if (!open.get()) {
            return;
        }

        try {
            receivedMessages.incrementAndGet();
            JsonNode node = objectMapper.readTree(message);
            String type = node.has("type") ? node.get("type").asText() : null;
            if (type == null) {
                ignoredMessages.incrementAndGet();
                return;
            }

            if ("GAME_OVER".equals(type)) {
                close();
                return;
            }

            if (!ACTIONABLE_MESSAGE_TYPES.contains(type)) {
                ignoredMessages.incrementAndGet();
                return;
            }

            if (GAME_STATE.equals(type)) {
                scheduleGameState(message);
            } else {
                scheduleMessage(type, message);
            }
        } catch (RejectedExecutionException e) {
            // close() may race with a final broadcast. A closed AI deliberately drops it.
            if (open.get()) {
                log.error("AI executor rejected message", e);
            }
        } catch (Exception e) {
            log.error("AI failed to parse message", e);
        }
    }

    /**
     * Coalesces repeated state broadcasts while preserving one follow-up decision when a
     * newer state arrives during the current decision. The decision engine reads live
     * GameData, so retaining every serialized GAME_STATE snapshot is both unnecessary and
     * actively harmful: bursts of broadcasts otherwise delay the current priority action.
     */
    private void scheduleGameState(String message) {
        synchronized (gameStateLock) {
            gameStateDirty = true;
            if (gameStateTaskScheduled) {
                coalescedGameStates.incrementAndGet();
                return;
            }
            gameStateTaskScheduled = true;
            scheduleGameStateTask(message);
        }
    }

    private void scheduleGameStateTask(String message) {
        long scheduledAtNanos = System.nanoTime();
        executor.schedule(() -> {
            synchronized (gameStateLock) {
                gameStateDirty = false;
            }
            handleScheduledMessage(GAME_STATE, message, scheduledAtNanos);

            synchronized (gameStateLock) {
                if (open.get() && gameStateDirty) {
                    scheduleGameStateTask(message);
                } else {
                    gameStateTaskScheduled = false;
                    gameStateDirty = false;
                }
            }
        }, decisionDelayMs, TimeUnit.MILLISECONDS);
    }

    private void scheduleMessage(String type, String message) {
        long scheduledAtNanos = System.nanoTime();
        executor.schedule(() -> handleScheduledMessage(type, message, scheduledAtNanos),
                decisionDelayMs, TimeUnit.MILLISECONDS);
    }

    private void handleScheduledMessage(String type, String message, long scheduledAtNanos) {
        if (!open.get()) {
            return;
        }

        long dispatchDelayMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - scheduledAtNanos)
                - decisionDelayMs;
        if (dispatchDelayMs >= SLOW_DISPATCH_THRESHOLD_MS) {
            log.warn("AI connection {} dispatch of {} waited {} ms beyond its decision delay; {}",
                    connectionId, type, dispatchDelayMs, diagnosticSummary());
        }

        activeMessageType = type;
        try {
            engine.handleMessage(type, message);
            handledMessages.incrementAndGet();
            lastHandledMessageType = type;
        } catch (Exception e) {
            log.error("AI decision error for message type {}", type, e);
        } finally {
            activeMessageType = null;
        }
    }

    @Override
    public void close() {
        open.set(false);
        // Use shutdown() rather than shutdownNow() to avoid setting the interrupt flag on the
        // calling thread. When close() is triggered by a GAME_OVER message the calling thread
        // may be the AI's own executor thread, and interrupting it would corrupt subsequent
        // blocking operations (e.g. WebSocket sends to human players in broadcastTournamentUpdate).
        // Already-queued tasks check open before invoking the decision engine.
        executor.shutdown();
    }

    public void scheduleInitialAction(Runnable action) {
        executor.schedule(() -> {
            if (!open.get()) {
                return;
            }
            try {
                action.run();
            } catch (Exception e) {
                log.error("AI initial action error", e);
            }
        }, decisionDelayMs, TimeUnit.MILLISECONDS);
    }

    public String diagnosticSummary() {
        synchronized (gameStateLock) {
            return "open=" + open.get()
                    + " queuedTasks=" + executor.getQueue().size()
                    + " active=" + activeMessageType
                    + " lastHandled=" + lastHandledMessageType
                    + " gameStateScheduled=" + gameStateTaskScheduled
                    + " gameStateDirty=" + gameStateDirty
                    + " received=" + receivedMessages.get()
                    + " handled=" + handledMessages.get()
                    + " ignored=" + ignoredMessages.get()
                    + " coalescedGameStates=" + coalescedGameStates.get();
        }
    }
}

