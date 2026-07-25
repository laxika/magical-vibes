package com.github.laxika.magicalvibes.testutil;

import com.github.laxika.magicalvibes.networking.Connection;
import com.github.laxika.magicalvibes.service.JacksonConfig;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class FakeConnection implements Connection {

    private static final ObjectMapper OBJECT_MAPPER = new JacksonConfig().objectMapper();

    private final String id;
    private boolean open = true;
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
        try {
            sentMessages.add(OBJECT_MAPPER.writeValueAsString(message));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize test message", e);
        }
    }

    @Override
    public void close() {
        open = false;
    }

    public List<String> getSentMessages() {
        return sentMessages;
    }

    public List<String> getMessagesContaining(String substring) {
        return sentMessages.stream()
                .filter(m -> m.contains(substring))
                .toList();
    }

    public void clearMessages() {
        sentMessages.clear();
    }
}

