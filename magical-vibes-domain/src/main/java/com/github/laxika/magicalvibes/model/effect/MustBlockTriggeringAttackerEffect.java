package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

/** When resolved, the target creature must block the permanent that caused the trigger this turn if able. */
public record MustBlockTriggeringAttackerEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature(), new PermanentIsCreaturePredicate());
    }
}
