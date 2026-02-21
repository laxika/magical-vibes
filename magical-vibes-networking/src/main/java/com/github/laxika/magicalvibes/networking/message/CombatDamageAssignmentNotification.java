package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.CombatDamageTargetView;
import com.github.laxika.magicalvibes.networking.model.MessageType;

import java.util.List;

public record CombatDamageAssignmentNotification(MessageType type, int attackerIndex,
    String attackerPermanentId, String attackerName, int totalDamage,
    List<CombatDamageTargetView> validTargets, boolean isTrample) {

    public CombatDamageAssignmentNotification(int attackerIndex, String attackerPermanentId,
            String attackerName, int totalDamage, List<CombatDamageTargetView> validTargets, boolean isTrample) {
        this(MessageType.COMBAT_DAMAGE_ASSIGNMENT, attackerIndex, attackerPermanentId,
                attackerName, totalDamage, validTargets, isTrample);
    }
}
