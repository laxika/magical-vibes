package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Removes all mire counters from the chosen land and records it as processed by one Tomb. */
public record RemoveMireCountersFromCyclopeanTombLandEffect(UUID tombPermanentId)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.land());
    }
}
