package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.MessageType;

import java.util.List;
import java.util.Map;

/**
 * Prompts a player to declare blocks. {@code choosingForOpponent} is true when the recipient is
 * choosing blocks for creatures they do not control (Melee) — the blocker indices then refer to the
 * opponent's battlefield and the attacker indices to the recipient's own.
 */
public record AvailableBlockersMessage(MessageType type, List<Integer> blockerIndices, List<Integer> attackerIndices,
                                       Map<Integer, List<Integer>> legalBlockPairs,
                                       List<Integer> mustBeBlockedAttackerIndices,
                                       List<Integer> menaceAttackerIndices,
                                       Map<Integer, List<Integer>> mustBlockRequirements,
                                       boolean choosingForOpponent) {
    public AvailableBlockersMessage(List<Integer> blockerIndices, List<Integer> attackerIndices,
                                    Map<Integer, List<Integer>> legalBlockPairs,
                                    List<Integer> mustBeBlockedAttackerIndices,
                                    List<Integer> menaceAttackerIndices,
                                    Map<Integer, List<Integer>> mustBlockRequirements,
                                    boolean choosingForOpponent) {
        this(MessageType.AVAILABLE_BLOCKERS, blockerIndices, attackerIndices, legalBlockPairs,
                mustBeBlockedAttackerIndices, menaceAttackerIndices, mustBlockRequirements,
                choosingForOpponent);
    }
}
