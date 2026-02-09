package com.github.laxika.magicalvibes.websocket;

import com.github.laxika.magicalvibes.networking.Connection;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@RequiredArgsConstructor
public class WebSocketConnection implements Connection {

    private final WebSocketSession session;

    @Override
    @SneakyThrows
    public void sendMessage(String message) {
        session.sendMessage(new TextMessage(message));
    }

    @Override
    @SneakyThrows
    public void close() {
        session.close();
    }
}
