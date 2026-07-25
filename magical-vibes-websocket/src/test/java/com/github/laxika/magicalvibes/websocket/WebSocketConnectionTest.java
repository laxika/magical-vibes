package com.github.laxika.magicalvibes.websocket;

import com.github.laxika.magicalvibes.networking.message.ErrorMessage;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class WebSocketConnectionTest {

    @Test
    void serializesTypedMessageAtWebSocketBoundary() {
        AtomicReference<TextMessage> sentMessage = new AtomicReference<>();
        WebSocketSession session = (WebSocketSession) Proxy.newProxyInstance(
                WebSocketSession.class.getClassLoader(),
                new Class<?>[]{WebSocketSession.class},
                (proxy, method, args) -> {
                    if ("sendMessage".equals(method.getName())) {
                        sentMessage.set((TextMessage) args[0]);
                    }
                    return null;
                });
        WebSocketConnection connection = new WebSocketConnection(session, new ObjectMapper());

        connection.sendMessage(new ErrorMessage("test"));

        assertThat(sentMessage.get().getPayload())
                .isEqualTo("{\"type\":\"ERROR\",\"message\":\"test\"}");
    }
}
