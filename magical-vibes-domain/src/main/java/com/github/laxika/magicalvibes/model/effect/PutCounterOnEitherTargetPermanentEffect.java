package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Puts one counter on one permanent chosen from the effect's two-target group at resolution.
 */
public record PutCounterOnEitherTargetPermanentEffect(CounterType counterType) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
