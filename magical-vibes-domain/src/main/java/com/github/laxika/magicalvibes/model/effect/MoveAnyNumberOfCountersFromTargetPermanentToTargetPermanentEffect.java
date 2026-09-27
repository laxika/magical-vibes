package com.github.laxika.magicalvibes.model.effect;

/** Moves any chosen number of counters of each kind from target permanent 0 onto target permanent 1. */
public record MoveAnyNumberOfCountersFromTargetPermanentToTargetPermanentEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
