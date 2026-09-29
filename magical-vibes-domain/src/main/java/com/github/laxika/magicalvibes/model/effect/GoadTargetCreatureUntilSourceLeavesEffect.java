package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Goads the targeted creature for as long as the source permanent remains on the battlefield. */
public record GoadTargetCreatureUntilSourceLeavesEffect()
        implements CombatAttackRequirementEffect, GoadStatusEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }

    @Override
    public PermanentPredicate affectedPredicate() {
        return new PermanentIsCreaturePredicate();
    }

    @Override
    public boolean requiresAttackAtOtherPlayerIfAble() {
        return true;
    }

    @Override
    public boolean makesGoaded() {
        return true;
    }
}
