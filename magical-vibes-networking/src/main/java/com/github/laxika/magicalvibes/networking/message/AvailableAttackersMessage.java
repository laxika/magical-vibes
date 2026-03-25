package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.MessageType;

import java.util.List;

public record AvailableAttackersMessage(MessageType type, List<Integer> attackerIndices, List<Integer> mustAttackIndices, List<AttackTarget> availableTargets, int taxPerCreature, boolean mustAttackWithAtLeastOne) {
    public AvailableAttackersMessage(List<Integer> attackerIndices, List<Integer> mustAttackIndices, List<AttackTarget> availableTargets, int taxPerCreature, boolean mustAttackWithAtLeastOne) {
        this(MessageType.AVAILABLE_ATTACKERS, attackerIndices, mustAttackIndices, availableTargets, taxPerCreature, mustAttackWithAtLeastOne);
    }

    /** Backward-compatible constructor without mustAttackWithAtLeastOne. */
    public AvailableAttackersMessage(List<Integer> attackerIndices, List<Integer> mustAttackIndices, List<AttackTarget> availableTargets, int taxPerCreature) {
        this(attackerIndices, mustAttackIndices, availableTargets, taxPerCreature, false);
    }
}
