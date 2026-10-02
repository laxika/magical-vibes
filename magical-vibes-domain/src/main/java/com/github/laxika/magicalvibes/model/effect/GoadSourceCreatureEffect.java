package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Static combat requirement that goads the source creature for as long as it remains on the battlefield. */
public record GoadSourceCreatureEffect()
        implements CombatAttackRequirementEffect, GoadStatusEffect {

    private static final PermanentPredicate SOURCE_CREATURE = new PermanentIsSourcePermanentPredicate();

    @Override
    public PermanentPredicate affectedPredicate() {
        return SOURCE_CREATURE;
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
