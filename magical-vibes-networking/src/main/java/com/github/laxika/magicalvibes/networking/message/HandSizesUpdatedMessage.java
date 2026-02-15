package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.MessageType;

import java.util.List;

public record HandSizesUpdatedMessage(MessageType type, List<Integer> handSizes) {
    public HandSizesUpdatedMessage(List<Integer> handSizes) {
        this(MessageType.HAND_SIZES_UPDATED, handSizes);
    }
}
