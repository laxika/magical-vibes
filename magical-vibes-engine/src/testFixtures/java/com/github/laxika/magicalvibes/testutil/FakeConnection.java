package com.github.laxika.magicalvibes.testutil;

import com.github.laxika.magicalvibes.networking.Connection;
import com.github.laxika.magicalvibes.service.JacksonConfig;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * In-memory connection that records every message sent to it as JSON.
 *
 * <p>Messages are serialized lazily, on the first read after they arrive: the engine projects a
 * state message for each player after every action, but only a few tests ever read them, so
 * eager serialization was a measurable share of every card test. Deferring it is safe because
 * outgoing messages are immutable view records.
 */
public class FakeConnection implements Connection {

    private static final ObjectMapper OBJECT_MAPPER = new JacksonConfig().objectMapper();

    private final String id;
    private boolean open = true;
    private final List<Object> unserializedMessages = new ArrayList<>();
    private final List<String> sentMessages = new ArrayList<>();

    public FakeConnection(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public void sendMessage(Object message) {
        unserializedMessages.add(message);
    }

    @Override
    public void close() {
        open = false;
    }

    public List<String> getSentMessages() {
        for (Object message : unserializedMessages) {
            try {
                sentMessages.add(OBJECT_MAPPER.writeValueAsString(message));
            } catch (Exception e) {
                throw new IllegalStateException("Failed to serialize test message", e);
            }
        }
        unserializedMessages.clear();
        return sentMessages;
    }

    public List<String> getMessagesContaining(String substring) {
        return getSentMessages().stream()
                .filter(m -> m.contains(substring))
                .toList();
    }

    public void clearMessages() {
        unserializedMessages.clear();
        sentMessages.clear();
    }
}
