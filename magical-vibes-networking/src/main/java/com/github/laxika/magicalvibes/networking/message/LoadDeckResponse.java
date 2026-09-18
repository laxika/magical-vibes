package com.github.laxika.magicalvibes.networking.message;
import com.github.laxika.magicalvibes.networking.model.MessageType;
public record LoadDeckResponse(MessageType type, SaveDeckRequest deck) {}
