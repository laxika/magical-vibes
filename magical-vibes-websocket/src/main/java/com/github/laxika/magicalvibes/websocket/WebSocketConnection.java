package com.github.laxika.magicalvibes.websocket;

import com.github.laxika.magicalvibes.networking.Connection;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
public class WebSocketConnection implements Connection {

    private final WebSocketSession session;
    private final ObjectMapper objectMapper;

    @Override
    public String getId() {
        return session.getId();
    }

    @Override
    public boolean isOpen() {
        return session.isOpen();
    }

    @Override
    @SneakyThrows
    public void sendMessage(Object message) {
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
    }

    @Override
    @SneakyThrows
    public void close() {
        session.close();
    }
}
