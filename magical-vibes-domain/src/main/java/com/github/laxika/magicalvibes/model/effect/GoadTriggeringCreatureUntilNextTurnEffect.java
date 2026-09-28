package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Goads the creature referenced by a triggered ability until its controller's next turn. */
public record GoadTriggeringCreatureUntilNextTurnEffect() implements CombatAttackRequirementEffect {

    @Override
    public PermanentPredicate affectedPredicate() {
        return new PermanentIsCreaturePredicate();
    }

    @Override
    public boolean requiresAttackAtOtherPlayerIfAble() {
        return true;
    }
}
