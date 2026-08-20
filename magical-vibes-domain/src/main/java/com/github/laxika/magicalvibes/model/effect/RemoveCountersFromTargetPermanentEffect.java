package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Removes up to the given number of counters of one type from a target permanent. */
public record RemoveCountersFromTargetPermanentEffect(CounterType counterType,
                                                       DynamicAmount amount,
                                                       PermanentPredicate targetPredicate)
        implements CardEffect {

    public RemoveCountersFromTargetPermanentEffect(CounterType counterType, int amount,
                                                    PermanentPredicate targetPredicate) {
        this(counterType, new Fixed(amount), targetPredicate);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent(), targetPredicate);
    }
}
