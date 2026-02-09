package com.github.laxika.magicalvibes.networking;

public interface Connection extends AutoCloseable {

    String getId();

    boolean isOpen();

    void sendMessage(String message);
}
