package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "Remove any number of counters from target permanent." The resolving controller chooses a
 * number from zero through the total number of counters on the target, and the chosen number may
 * include counters of different kinds.
 */
public record RemoveAnyNumberOfCountersFromTargetPermanentEffect(PermanentPredicate targetPredicate)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature(), targetPredicate);
    }
}
