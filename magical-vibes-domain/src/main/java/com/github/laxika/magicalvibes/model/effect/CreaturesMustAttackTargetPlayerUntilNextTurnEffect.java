package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.UUID;

/**
 * Floating combat requirement used for a creature that must attack a specified player each combat
 * if able.
 */
public record CreaturesMustAttackTargetPlayerUntilNextTurnEffect(UUID targetPlayerId)
        implements CombatAttackRequirementEffect {

    private static final PermanentPredicate CREATURE = new PermanentIsCreaturePredicate();

    @Override
    public PermanentPredicate affectedPredicate() {
        return CREATURE;
    }

    @Override
    public UUID requiredAttackTargetId(GameData gameData, Permanent sourcePermanent) {
        return targetPlayerId;
    }
}
