package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.MessageType;

import java.util.List;

public record AvailableBlockersMessage(MessageType type, List<Integer> blockerIndices, List<Integer> attackerIndices) {
    public AvailableBlockersMessage(List<Integer> blockerIndices, List<Integer> attackerIndices) {
        this(MessageType.AVAILABLE_BLOCKERS, blockerIndices, attackerIndices);
    }
}
