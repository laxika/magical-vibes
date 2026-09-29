package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Puts counters on matching permanents and goads exactly the permanents that received one. */
public record PutCounterOnEachMatchingPermanentThenGoadEffect(
        CounterType counterType,
        int count,
        PermanentPredicate predicate,
        EachPermanentScope scope
) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return scope == EachPermanentScope.TARGET_PLAYER
                ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
