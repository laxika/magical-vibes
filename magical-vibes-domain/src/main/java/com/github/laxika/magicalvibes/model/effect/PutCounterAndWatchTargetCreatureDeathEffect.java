package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Puts a counter on a target creature and watches that exact permanent for the rest of the turn. */
public record PutCounterAndWatchTargetCreatureDeathEffect(
        CounterType counterType,
        PermanentPredicate targetRestriction,
        CardEffect deathEffect
) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature(), targetRestriction);
    }
}
