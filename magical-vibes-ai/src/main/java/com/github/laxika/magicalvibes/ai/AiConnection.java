package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.networking.Connection;
import com.github.laxika.magicalvibes.networking.message.AvailableAttackersMessage;
import com.github.laxika.magicalvibes.networking.message.AvailableBlockersMessage;
import com.github.laxika.magicalvibes.networking.message.CombatDamageAssignmentNotification;
import com.github.laxika.magicalvibes.networking.message.GameOverMessage;
import com.github.laxika.magicalvibes.networking.message.GameStateMessage;
import com.github.laxika.magicalvibes.networking.message.InteractionPromptMessage;
import com.github.laxika.magicalvibes.networking.message.MulliganResolvedMessage;
import com.github.laxika.magicalvibes.networking.message.SelectCardsToBottomMessage;
import com.github.laxika.magicalvibes.networking.model.MessageType;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class AiConnection implements Connection {

    private static final long DEFAULT_DECISION_DELAY_MS = 800;
    private static final long SLOW_DISPATCH_THRESHOLD_MS = 1_000;

    private final String connectionId;
    private final AiDecisionEngine engine;
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
    private volatile MessageType activeMessageType;
    private volatile MessageType lastHandledMessageType;

    public AiConnection(String connectionId, AiDecisionEngine engine) {
        this(connectionId, engine, DEFAULT_DECISION_DELAY_MS);
    }

    public AiConnection(String connectionId, AiDecisionEngine engine, long decisionDelayMs) {
        this.connectionId = connectionId;
        this.engine = engine;
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
    public void sendMessage(Object message) {
        if (!open.get()) {
            return;
        }

        try {
            receivedMessages.incrementAndGet();
            if (message instanceof GameOverMessage) {
                close();
                return;
            }

            MessageType type = actionableType(message);
            if (type == null) {
                ignoredMessages.incrementAndGet();
                return;
            }

            if (type == MessageType.GAME_STATE) {
                scheduleGameState();
            } else {
                scheduleMessage(type);
            }
        } catch (RejectedExecutionException e) {
            // close() may race with a final broadcast. A closed AI deliberately drops it.
            if (open.get()) {
                log.error("AI executor rejected message", e);
            }
        }
    }

    private MessageType actionableType(Object message) {
        if (message instanceof GameStateMessage) {
            return MessageType.GAME_STATE;
        }
        if (message instanceof MulliganResolvedMessage) {
            return MessageType.MULLIGAN_RESOLVED;
        }
        if (message instanceof SelectCardsToBottomMessage) {
            return MessageType.SELECT_CARDS_TO_BOTTOM;
        }
        if (message instanceof AvailableAttackersMessage) {
            return MessageType.AVAILABLE_ATTACKERS;
        }
        if (message instanceof AvailableBlockersMessage) {
            return MessageType.AVAILABLE_BLOCKERS;
        }
        if (message instanceof InteractionPromptMessage) {
            return MessageType.INTERACTION_PROMPT;
        }
        if (message instanceof CombatDamageAssignmentNotification) {
            return MessageType.COMBAT_DAMAGE_ASSIGNMENT;
        }
        return null;
    }

    /**
     * Coalesces repeated state broadcasts while preserving one follow-up decision when a
     * newer state arrives during the current decision. The decision engine reads live
     * GameData, so retaining every {@link GameStateMessage} is both unnecessary and
     * actively harmful: bursts of broadcasts otherwise delay the current priority action.
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
            handleScheduledEvent(MessageType.GAME_STATE, scheduledAtNanos);

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

    private void scheduleMessage(MessageType type) {
        long scheduledAtNanos = System.nanoTime();
        executor.schedule(() -> handleScheduledEvent(type, scheduledAtNanos),
                decisionDelayMs, TimeUnit.MILLISECONDS);
    }

    private void handleScheduledEvent(MessageType type, long scheduledAtNanos) {
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
            engine.handleEvent(type);
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

