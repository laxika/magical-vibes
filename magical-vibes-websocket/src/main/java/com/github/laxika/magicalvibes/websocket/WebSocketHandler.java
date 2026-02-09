package com.github.laxika.magicalvibes.websocket;

import com.github.laxika.magicalvibes.networking.MessageHandler;
import com.github.laxika.magicalvibes.networking.model.MessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.concurrent.*;

@Slf4j
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {

    private static final int TIMEOUT_SECONDS = 3;

    private final ObjectMapper objectMapper;
    private final MessageHandler messageHandler;
    private final WebSocketSessionManager sessionManager;
    private final ConcurrentHashMap<String, ScheduledFuture<?>> timeoutTasks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("WebSocket connection established: {}", session.getId());

        ScheduledFuture<?> timeoutTask = scheduler.schedule(() -> {
            try {
                if (session.isOpen()) {
                    log.warn("Connection timeout for session: {}", session.getId());
                }
            } finally {
                timeoutTasks.remove(session.getId());
            }
        }, TIMEOUT_SECONDS, TimeUnit.SECONDS);

        timeoutTasks.put(session.getId(), timeoutTask);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.debug("Received message from session {}: {}", session.getId(), message.getPayload());

        // Cancel timeout task on first message
        ScheduledFuture<?> timeoutTask = timeoutTasks.remove(session.getId());
        if (timeoutTask != null) {
            timeoutTask.cancel(false);
        }

        try {
            JsonNode jsonNode = objectMapper.readTree(message.getPayload());
            MessageType type = MessageType.valueOf(jsonNode.get("type").asString());

            switch (type) {
                case LOGIN -> handleLogin(session, jsonNode);
                case CREATE_GAME -> handleCreateGame(session, jsonNode);
                case JOIN_GAME -> handleJoinGame(session, jsonNode);
                case PASS_PRIORITY -> handlePassPriority(session, jsonNode);
                case KEEP_HAND -> handleKeepHand(session, jsonNode);
                case TAKE_MULLIGAN -> handleMulligan(session, jsonNode);
                case BOTTOM_CARDS -> handleBottomCards(session, jsonNode);
                case PLAY_CARD -> handlePlayCard(session, jsonNode);
                case TAP_PERMANENT -> handleTapPermanent(session, jsonNode);
                case SET_AUTO_STOPS -> handleSetAutoStops(session, jsonNode);
                case DECLARE_ATTACKERS -> handleDeclareAttackers(session, jsonNode);
                case DECLARE_BLOCKERS -> handleDeclareBlockers(session, jsonNode);
                case CARD_CHOSEN -> handleCardChosen(session, jsonNode);
                default -> sendError(session, "Unknown message type: " + type);
            }
        } catch (Exception e) {
            log.error("Error processing message", e);
            sendError(session, "Error processing request");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("WebSocket connection closed: {} with status: {}", session.getId(), status);
        sessionManager.unregisterSession(session.getId());

        ScheduledFuture<?> task = timeoutTasks.remove(session.getId());
        if (task != null) {
            task.cancel(false);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket transport error for session: {}", session.getId(), exception);
        sessionManager.unregisterSession(session.getId());

        ScheduledFuture<?> task = timeoutTasks.remove(session.getId());
        if (task != null) {
            task.cancel(false);
        }
    }
}
