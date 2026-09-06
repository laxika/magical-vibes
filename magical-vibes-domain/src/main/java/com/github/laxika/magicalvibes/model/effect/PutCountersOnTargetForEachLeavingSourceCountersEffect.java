package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Map;

/**
 * Leaves-the-battlefield trigger that puts the same number of each counter type on a target
 * permanent as the source had before leaving.
 *
 * @param counters        snapshot of the source permanent's counters, keyed by counter type
 * @param targetPredicate optional additional predicate for the target permanent
 */
public record PutCountersOnTargetForEachLeavingSourceCountersEffect(
        Map<CounterType, Integer> counters,
        PermanentPredicate targetPredicate
) implements CardEffect {

    public PutCountersOnTargetForEachLeavingSourceCountersEffect {
        counters = Map.copyOf(counters);
    }

    public PutCountersOnTargetForEachLeavingSourceCountersEffect() {
        this(Map.of(), null);
    }

    public PutCountersOnTargetForEachLeavingSourceCountersEffect(PermanentPredicate targetPredicate) {
        this(Map.of(), targetPredicate);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent(),
                targetPredicate != null ? targetPredicate : new PermanentIsCreaturePredicate());
    }
}
