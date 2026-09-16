package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.XValue;

/** Moves a dynamic number of counters of one type from the source permanent onto the target. */
public record MoveCountersFromSourceToTargetPermanentEffect(
        CounterType counterType,
        DynamicAmount amount
) implements CardEffect {

    public MoveCountersFromSourceToTargetPermanentEffect(CounterType counterType) {
        this(counterType, new XValue());
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
