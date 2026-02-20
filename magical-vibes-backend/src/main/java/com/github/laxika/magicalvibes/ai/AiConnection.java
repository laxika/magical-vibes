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

    private static final long DECISION_DELAY_MS = 800;

    private final String connectionId;
    private final AiDecisionEngine engine;
    private final ObjectMapper objectMapper;
    private final ScheduledExecutorService executor;
    private final AtomicBoolean open = new AtomicBoolean(true);

    public AiConnection(String connectionId, AiDecisionEngine engine, ObjectMapper objectMapper) {
        this.connectionId = connectionId;
        this.engine = engine;
        this.objectMapper = objectMapper;

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

            executor.schedule(() -> {
                try {
                    engine.handleMessage(type, message);
                } catch (Exception e) {
                    log.error("AI decision error for message type {}", type, e);
                }
            }, DECISION_DELAY_MS, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("AI failed to parse message", e);
        }
    }

    @Override
    public void close() {
        open.set(false);
        executor.shutdownNow();
    }

    public void scheduleInitialAction(Runnable action) {
        executor.schedule(() -> {
            try {
                action.run();
            } catch (Exception e) {
                log.error("AI initial action error", e);
            }
        }, DECISION_DELAY_MS, TimeUnit.MILLISECONDS);
    }
}

