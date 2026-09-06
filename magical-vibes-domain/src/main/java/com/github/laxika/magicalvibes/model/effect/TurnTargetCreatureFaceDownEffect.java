package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Turns the target creature face down as a 2/2 creature. */
public record TurnTargetCreatureFaceDownEffect(PermanentPredicate targetFilter) implements CardEffect {

    public TurnTargetCreatureFaceDownEffect() {
        this(null);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetFilter == null
                ? TargetSpec.benign(TargetPredicates.creature())
                : TargetSpec.benign(TargetPredicates.creature(), targetFilter);
    }
}
