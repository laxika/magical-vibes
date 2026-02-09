package com.github.laxika.magicalvibes.dto;

import com.github.laxika.magicalvibes.networking.model.MessageType;

import java.util.Map;

public record ManaUpdatedMessage(MessageType type, Map<String, Integer> manaPool) {
    public ManaUpdatedMessage(Map<String, Integer> manaPool) {
        this(MessageType.MANA_UPDATED, manaPool);
    }
}
