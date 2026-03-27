package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.networking.Connection;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class AiConnection implements Connection {

    private static final long DEFAULT_DECISION_DELAY_MS = 800;

    private final String connectionId;
    private final AiDecisionEngine engine;
    private final ObjectMapper objectMapper;
    private final ScheduledExecutorService executor;
    private final long decisionDelayMs;
    private final AtomicBoolean open = new AtomicBoolean(true);

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
            JsonNode node = objectMapper.readTree(message);
            String type = node.has("type") ? node.get("type").asText() : null;
            if (type == null) {
                return;
            }

            if ("GAME_OVER".equals(type)) {
                close();
                return;
            }

            executor.schedule(() -> {
                try {
                    engine.handleMessage(type, message);
                } catch (Exception e) {
                    log.error("AI decision error for message type {}", type, e);
                }
            }, decisionDelayMs, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("AI failed to parse message", e);
        }
    }

    @Override
    public void close() {
        open.set(false);
        // Use shutdown() rather than shutdownNow() to avoid setting the interrupt flag on the
        // calling thread. When close() is triggered by a GAME_OVER message the calling thread
        // may be the AI's own executor thread, and interrupting it would corrupt subsequent
        // blocking operations (e.g. WebSocket sends to human players in broadcastTournamentUpdate).
        // Any already-queued tasks will exit immediately because AiDecisionEngine.handleMessage
        // checks gameData.status == FINISHED at the top.
        executor.shutdown();
    }

    public void scheduleInitialAction(Runnable action) {
        executor.schedule(() -> {
            try {
                action.run();
            } catch (Exception e) {
                log.error("AI initial action error", e);
            }
        }, decisionDelayMs, TimeUnit.MILLISECONDS);
    }
}

