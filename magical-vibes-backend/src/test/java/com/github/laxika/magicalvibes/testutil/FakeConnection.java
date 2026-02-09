package com.github.laxika.magicalvibes.testutil;

import com.github.laxika.magicalvibes.networking.Connection;

import java.util.ArrayList;
import java.util.List;

public class FakeConnection implements Connection {

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
    public void sendMessage(String message) {
        sentMessages.add(message);
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
