package com.github.laxika.magicalvibes.websocket;

import com.github.laxika.magicalvibes.networking.Connection;
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

        Connection connection = new WebSocketConnection(session);

        ScheduledFuture<?> timeoutTask = scheduler.schedule(() -> {
            try {
                if (connection.isOpen()) {
                    log.warn("Connection timeout for session: {}", session.getId());
                    messageHandler.handleTimeout(connection);
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

        Connection connection = new WebSocketConnection(session);

        try {
            JsonNode jsonNode = objectMapper.readTree(message.getPayload());
            MessageType type = MessageType.valueOf(jsonNode.get("type").asString());

            switch (type) {
                case LOGIN -> messageHandler.handleLogin(connection, jsonNode);
                case CREATE_GAME -> messageHandler.handleCreateGame(connection, jsonNode);
                case JOIN_GAME -> messageHandler.handleJoinGame(connection, jsonNode);
                case PASS_PRIORITY -> messageHandler.handlePassPriority(connection, jsonNode);
                case KEEP_HAND -> messageHandler.handleKeepHand(connection, jsonNode);
                case TAKE_MULLIGAN -> messageHandler.handleMulligan(connection, jsonNode);
                case BOTTOM_CARDS -> messageHandler.handleBottomCards(connection, jsonNode);
                case PLAY_CARD -> messageHandler.handlePlayCard(connection, jsonNode);
                case TAP_PERMANENT -> messageHandler.handleTapPermanent(connection, jsonNode);
                case SET_AUTO_STOPS -> messageHandler.handleSetAutoStops(connection, jsonNode);
                case DECLARE_ATTACKERS -> messageHandler.handleDeclareAttackers(connection, jsonNode);
                case DECLARE_BLOCKERS -> messageHandler.handleDeclareBlockers(connection, jsonNode);
                case CARD_CHOSEN -> messageHandler.handleCardChosen(connection, jsonNode);
                default -> messageHandler.handleError(connection, "Unknown message type: " + type);
            }
        } catch (Exception e) {
            log.error("Error processing message", e);
            try {
                messageHandler.handleError(connection, "Error processing request");
            } catch (Exception ex) {
                log.error("Error sending error message", ex);
            }
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
