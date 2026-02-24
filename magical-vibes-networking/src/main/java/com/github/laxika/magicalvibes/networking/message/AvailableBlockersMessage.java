package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.MessageType;

import java.util.List;
import java.util.Map;

public record AvailableBlockersMessage(MessageType type, List<Integer> blockerIndices, List<Integer> attackerIndices,
                                       Map<Integer, List<Integer>> legalBlockPairs) {
    public AvailableBlockersMessage(List<Integer> blockerIndices, List<Integer> attackerIndices,
                                    Map<Integer, List<Integer>> legalBlockPairs) {
        this(MessageType.AVAILABLE_BLOCKERS, blockerIndices, attackerIndices, legalBlockPairs);
    }
}
