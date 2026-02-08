package com.github.laxika.magicalvibes.dto;

import com.github.laxika.magicalvibes.model.MessageType;

import java.util.List;

public record ChooseCreatureFromHandMessage(MessageType type, List<Integer> creatureIndices) {

    public ChooseCreatureFromHandMessage(List<Integer> creatureIndices) {
        this(MessageType.CHOOSE_CREATURE_FROM_HAND, creatureIndices);
    }
}
