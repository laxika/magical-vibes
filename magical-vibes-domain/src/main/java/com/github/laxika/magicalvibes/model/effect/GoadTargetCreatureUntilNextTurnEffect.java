package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Goads the targeted creature until the ability controller's next turn. */
public record GoadTargetCreatureUntilNextTurnEffect(PermanentPredicate targetRestriction)
        implements CombatAttackRequirementEffect {

    public GoadTargetCreatureUntilNextTurnEffect() {
        this(null);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature(), targetRestriction);
    }

    @Override
    public PermanentPredicate affectedPredicate() {
        return new PermanentIsCreaturePredicate();
    }

    @Override
    public boolean requiresAttackAtOtherPlayerIfAble() {
        return true;
    }
}
