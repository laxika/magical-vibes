package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.MessageType;

import java.util.List;
import java.util.Map;

public record AvailableBlockersMessage(MessageType type, List<Integer> blockerIndices, List<Integer> attackerIndices,
                                       Map<Integer, List<Integer>> legalBlockPairs,
                                       List<Integer> mustBeBlockedAttackerIndices,
                                       List<Integer> menaceAttackerIndices,
                                       Map<Integer, List<Integer>> mustBlockRequirements) {
    public AvailableBlockersMessage(List<Integer> blockerIndices, List<Integer> attackerIndices,
                                    Map<Integer, List<Integer>> legalBlockPairs,
                                    List<Integer> mustBeBlockedAttackerIndices,
                                    List<Integer> menaceAttackerIndices,
                                    Map<Integer, List<Integer>> mustBlockRequirements) {
        this(MessageType.AVAILABLE_BLOCKERS, blockerIndices, attackerIndices, legalBlockPairs,
                mustBeBlockedAttackerIndices, menaceAttackerIndices, mustBlockRequirements);
    }
}
