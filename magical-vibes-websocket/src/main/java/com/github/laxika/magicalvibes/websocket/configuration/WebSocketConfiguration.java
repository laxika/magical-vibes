package com.github.laxika.magicalvibes.websocket.configuration;

import com.github.laxika.magicalvibes.networking.MessageHandler;
import com.github.laxika.magicalvibes.websocket.WebSocketHandler;
import com.github.laxika.magicalvibes.websocket.WebSocketSessionManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import tools.jackson.databind.ObjectMapper;

/**
 * Spring wiring owned by the websocket module: the {@code SessionManager} implementation and the
 * Spring WebSocket handler registration. Scans the module's own package so downstream modules can
 * compose it via {@code @Import} rather than reaching in with their own component scan.
 */
@Configuration
@EnableWebSocket
@ComponentScan("com.github.laxika.magicalvibes.websocket")
public class WebSocketConfiguration implements WebSocketConfigurer {

    private final MessageHandler messageHandler;
    private final ObjectMapper objectMapper;
    private final WebSocketSessionManager sessionManager;

    public WebSocketConfiguration(MessageHandler messageHandler, ObjectMapper objectMapper, WebSocketSessionManager sessionManager) {
        this.messageHandler = messageHandler;
        this.objectMapper = objectMapper;
        this.sessionManager = sessionManager;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new WebSocketHandler(objectMapper, messageHandler, sessionManager), "/ws/login")
                .setAllowedOrigins("*");
    }
}
