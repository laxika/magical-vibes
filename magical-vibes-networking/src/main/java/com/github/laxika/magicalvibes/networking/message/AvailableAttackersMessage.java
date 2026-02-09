package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.MessageType;

import java.util.List;

public record AvailableAttackersMessage(MessageType type, List<Integer> attackerIndices) {
    public AvailableAttackersMessage(List<Integer> attackerIndices) {
        this(MessageType.AVAILABLE_ATTACKERS, attackerIndices);
    }
}
