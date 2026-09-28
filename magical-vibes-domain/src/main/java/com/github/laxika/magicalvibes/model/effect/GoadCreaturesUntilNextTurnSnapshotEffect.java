package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Goads the matching creatures that exist as this effect resolves until the controller's next turn. */
public record GoadCreaturesUntilNextTurnSnapshotEffect(PermanentPredicate affectedPredicate)
        implements CombatAttackRequirementEffect, GoadStatusEffect {

    @Override
    public boolean requiresAttackAtOtherPlayerIfAble() {
        return true;
    }

    @Override
    public boolean makesGoaded() {
        return true;
    }
}
