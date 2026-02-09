package com.github.laxika.magicalvibes.networking;

public interface Connection extends AutoCloseable {

    void sendMessage(String message);
}
