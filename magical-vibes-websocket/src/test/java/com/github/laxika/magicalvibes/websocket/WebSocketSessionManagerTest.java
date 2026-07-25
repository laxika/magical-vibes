package com.github.laxika.magicalvibes.websocket;

import com.github.laxika.magicalvibes.networking.Connection;
import com.github.laxika.magicalvibes.networking.message.ErrorMessage;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WebSocketSessionManagerTest {

    @Test
    void passesTypedMessageToConnectionWithoutSerializingIt() {
        WebSocketSessionManager sessionManager = new WebSocketSessionManager();
        CapturingConnection connection = new CapturingConnection("test");
        UUID playerId = UUID.randomUUID();
        ErrorMessage message = new ErrorMessage("test");
        sessionManager.registerPlayer(connection, playerId, "Player");

        sessionManager.sendToPlayer(playerId, message);

        assertThat(connection.sentMessage).isSameAs(message);
    }

    private static class CapturingConnection implements Connection {

        private final String id;
        private Object sentMessage;

        private CapturingConnection(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public boolean isOpen() {
            return true;
        }

        @Override
        public void sendMessage(Object message) {
            sentMessage = message;
        }

        @Override
        public void close() {
        }
    }
}
