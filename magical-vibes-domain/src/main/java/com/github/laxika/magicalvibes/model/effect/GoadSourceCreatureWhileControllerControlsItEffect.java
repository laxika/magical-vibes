package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Goads the source creature for as long as its current controller controls it. */
public record GoadSourceCreatureWhileControllerControlsItEffect()
        implements CombatAttackRequirementEffect {

    @Override
    public PermanentPredicate affectedPredicate() {
        return new PermanentIsCreaturePredicate();
    }

    @Override
    public boolean endsWhenSourceControllerChanges() {
        return true;
    }

    @Override
    public boolean requiresAttackAtOtherPlayerIfAble() {
        return true;
    }
}
