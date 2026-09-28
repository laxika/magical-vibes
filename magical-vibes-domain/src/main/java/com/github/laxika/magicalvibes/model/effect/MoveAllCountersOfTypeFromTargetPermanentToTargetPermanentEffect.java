package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/** Moves all counters of one type from the first target permanent onto the second. */
public record MoveAllCountersOfTypeFromTargetPermanentToTargetPermanentEffect(CounterType counterType)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
