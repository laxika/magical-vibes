package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.MessageType;

public record SelectCardsToBottomMessage(MessageType type, int count) {
    public SelectCardsToBottomMessage(int count) {
        this(MessageType.SELECT_CARDS_TO_BOTTOM, count);
    }
}
