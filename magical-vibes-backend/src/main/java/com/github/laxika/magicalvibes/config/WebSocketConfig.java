package com.github.laxika.magicalvibes.config;

import com.github.laxika.magicalvibes.handler.LoginWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final LoginWebSocketHandler loginWebSocketHandler;

    public WebSocketConfig(LoginWebSocketHandler loginWebSocketHandler) {
        this.loginWebSocketHandler = loginWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(loginWebSocketHandler, "/ws/login")
                .setAllowedOrigins("http://localhost:4200");
    }
}
