package com.github.laxika.magicalvibes.dto;

import com.github.laxika.magicalvibes.model.MessageType;

import java.util.List;

public record AvailableAttackersMessage(MessageType type, List<Integer> attackerIndices) {
    public AvailableAttackersMessage(List<Integer> attackerIndices) {
        this(MessageType.AVAILABLE_ATTACKERS, attackerIndices);
    }
}
