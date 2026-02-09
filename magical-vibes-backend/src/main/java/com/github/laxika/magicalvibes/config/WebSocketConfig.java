package com.github.laxika.magicalvibes.config;

import com.github.laxika.magicalvibes.networking.MessageHandler;
import com.github.laxika.magicalvibes.websocket.WebSocketHandler;
import com.github.laxika.magicalvibes.websocket.WebSocketSessionManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final MessageHandler messageHandler;
    private final ObjectMapper objectMapper;
    private final WebSocketSessionManager sessionManager;

    public WebSocketConfig(MessageHandler messageHandler, ObjectMapper objectMapper, WebSocketSessionManager sessionManager) {
        this.messageHandler = messageHandler;
        this.objectMapper = objectMapper;
        this.sessionManager = sessionManager;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new WebSocketHandler(objectMapper, messageHandler, sessionManager), "/ws/login")
                .setAllowedOrigins("http://localhost:4200");
    }
}
