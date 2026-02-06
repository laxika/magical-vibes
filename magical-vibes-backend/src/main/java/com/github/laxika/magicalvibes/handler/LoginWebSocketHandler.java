package com.github.laxika.magicalvibes.handler;

import com.github.laxika.magicalvibes.dto.GameResponse;
import com.github.laxika.magicalvibes.dto.LoginRequest;
import com.github.laxika.magicalvibes.dto.LoginResponse;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.service.LoginService;
import com.github.laxika.magicalvibes.service.WebSocketSessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

@Component
@Slf4j
public class LoginWebSocketHandler extends TextWebSocketHandler {

    private static final int TIMEOUT_SECONDS = 3;

    private final LoginService loginService;
    private final GameService gameService;
    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;
    private final ScheduledExecutorService scheduler;
    private final ConcurrentHashMap<String, ScheduledFuture<?>> timeoutTasks;

    public LoginWebSocketHandler(LoginService loginService,
                                 GameService gameService,
                                 WebSocketSessionManager sessionManager) {
        this.loginService = loginService;
        this.gameService = gameService;
        this.sessionManager = sessionManager;
        this.objectMapper = new ObjectMapper();
        this.scheduler = Executors.newScheduledThreadPool(10);
        this.timeoutTasks = new ConcurrentHashMap<>();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket connection established: {}", session.getId());

        ScheduledFuture<?> timeoutTask = scheduler.schedule(() -> {
            try {
                if (session.isOpen()) {
                    log.warn("Connection timeout for session: {}", session.getId());
                    LoginResponse timeoutResponse = LoginResponse.timeout();
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(timeoutResponse)));
                    session.close(CloseStatus.NORMAL);
                }
            } catch (IOException e) {
                log.error("Error sending timeout message", e);
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
            String type = jsonNode.get("type").asText();

            switch (type) {
                case "LOGIN" -> handleLogin(session, jsonNode);
                case "CREATE_GAME" -> handleCreateGame(session, jsonNode);
                case "JOIN_GAME" -> handleJoinGame(session, jsonNode);
                default -> sendError(session, "Unknown message type: " + type);
            }
        } catch (Exception e) {
            log.error("Error processing message", e);
            sendError(session, "Error processing request");
        }
    }

    private void handleLogin(WebSocketSession session, JsonNode jsonNode) throws IOException {
        LoginRequest loginRequest = objectMapper.treeToValue(jsonNode, LoginRequest.class);
        LoginResponse response = loginService.authenticate(loginRequest);

        String jsonResponse = objectMapper.writeValueAsString(response);
        session.sendMessage(new TextMessage(jsonResponse));
        log.info("Sent login response to session {}: {}", session.getId(), response.getType());

        if ("LOGIN_SUCCESS".equals(response.getType())) {
            sessionManager.registerSession(session, response.getUserId());
            log.info("Session {} registered for user {} - connection staying open", session.getId(), response.getUserId());
        } else {
            session.close(CloseStatus.NORMAL);
        }
    }

    private void handleCreateGame(WebSocketSession session, JsonNode jsonNode) throws IOException {
        Long userId = sessionManager.getUserId(session.getId());
        if (userId == null) {
            sendError(session, "Not authenticated");
            return;
        }

        String gameName = jsonNode.get("gameName").asText();
        GameResponse gameResponse = gameService.createGame(gameName, userId);

        // Broadcast NEW_GAME to all connected users
        broadcastToAll("NEW_GAME", gameResponse);
    }

    private void handleJoinGame(WebSocketSession session, JsonNode jsonNode) throws IOException {
        Long userId = sessionManager.getUserId(session.getId());
        if (userId == null) {
            sendError(session, "Not authenticated");
            return;
        }

        Long gameId = jsonNode.get("gameId").asLong();

        try {
            GameResponse gameResponse = gameService.joinGame(gameId, userId);
            broadcastToAll("GAME_UPDATED", gameResponse);
        } catch (IllegalArgumentException | IllegalStateException e) {
            sendError(session, e.getMessage());
        }
    }

    private void broadcastToAll(String type, GameResponse game) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", type);
        notification.put("game", game);

        Map<String, WebSocketSession> sessions = sessionManager.getAllSessions();
        int sentCount = 0;

        for (Map.Entry<String, WebSocketSession> entry : sessions.entrySet()) {
            try {
                WebSocketSession s = entry.getValue();
                if (s.isOpen()) {
                    String msg = objectMapper.writeValueAsString(notification);
                    s.sendMessage(new TextMessage(msg));
                    sentCount++;
                }
            } catch (Exception e) {
                log.error("Error sending notification to session: {}", entry.getKey(), e);
            }
        }

        log.info("Broadcasted {} to {} users", type, sentCount);
    }

    private void sendError(WebSocketSession session, String message) throws IOException {
        Map<String, String> error = Map.of("type", "ERROR", "message", message);
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(error)));
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
