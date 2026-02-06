package com.github.laxika.magicalvibes.handler;

import com.github.laxika.magicalvibes.dto.LoginRequest;
import com.github.laxika.magicalvibes.dto.LoginResponse;
import com.github.laxika.magicalvibes.service.LoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.concurrent.*;

@Component
public class LoginWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(LoginWebSocketHandler.class);
    private static final int TIMEOUT_SECONDS = 3;

    private final LoginService loginService;
    private final ObjectMapper objectMapper;
    private final ScheduledExecutorService scheduler;
    private final ConcurrentHashMap<String, ScheduledFuture<?>> timeoutTasks;

    public LoginWebSocketHandler(LoginService loginService) {
        this.loginService = loginService;
        this.objectMapper = new ObjectMapper();
        this.scheduler = Executors.newScheduledThreadPool(10);
        this.timeoutTasks = new ConcurrentHashMap<>();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("WebSocket connection established: {}", session.getId());

        // Schedule timeout task
        ScheduledFuture<?> timeoutTask = scheduler.schedule(() -> {
            try {
                if (session.isOpen()) {
                    logger.warn("Connection timeout for session: {}", session.getId());
                    LoginResponse timeoutResponse = LoginResponse.timeout();
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(timeoutResponse)));
                    session.close(CloseStatus.NORMAL);
                }
            } catch (IOException e) {
                logger.error("Error sending timeout message", e);
            } finally {
                timeoutTasks.remove(session.getId());
            }
        }, TIMEOUT_SECONDS, TimeUnit.SECONDS);

        timeoutTasks.put(session.getId(), timeoutTask);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        logger.debug("Received message from session {}: {}", session.getId(), message.getPayload());

        // Cancel timeout task since we received a message
        ScheduledFuture<?> timeoutTask = timeoutTasks.remove(session.getId());
        if (timeoutTask != null) {
            timeoutTask.cancel(false);
            logger.debug("Cancelled timeout task for session: {}", session.getId());
        }

        try {
            // Parse login request
            LoginRequest loginRequest = objectMapper.readValue(message.getPayload(), LoginRequest.class);

            // Validate message type
            if (!"LOGIN".equals(loginRequest.getType())) {
                logger.warn("Invalid message type received: {}", loginRequest.getType());
                sendResponse(session, LoginResponse.failure("Invalid message type"));
                return;
            }

            // Authenticate user
            LoginResponse response = loginService.authenticate(loginRequest);
            sendResponse(session, response);

        } catch (Exception e) {
            logger.error("Error processing login request", e);
            sendResponse(session, LoginResponse.failure("Error processing login request"));
        }
    }

    private void sendResponse(WebSocketSession session, LoginResponse response) throws IOException {
        String jsonResponse = objectMapper.writeValueAsString(response);
        session.sendMessage(new TextMessage(jsonResponse));
        logger.info("Sent response to session {}: {}", session.getId(), response.getType());

        // Close connection after sending response
        session.close(CloseStatus.NORMAL);
        logger.info("Closed connection for session: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        logger.info("WebSocket connection closed: {} with status: {}", session.getId(), status);

        // Clean up timeout task if still present
        ScheduledFuture<?> timeoutTask = timeoutTasks.remove(session.getId());
        if (timeoutTask != null) {
            timeoutTask.cancel(false);
            logger.debug("Cleaned up timeout task for session: {}", session.getId());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        logger.error("WebSocket transport error for session: {}", session.getId(), exception);

        // Clean up timeout task
        ScheduledFuture<?> timeoutTask = timeoutTasks.remove(session.getId());
        if (timeoutTask != null) {
            timeoutTask.cancel(false);
        }
    }
}
