package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;

/** Adds or removes time counters from a target permanent or suspended card. */
public record AdjustTimeCountersOnTargetEffect(boolean add, DynamicAmount amount) implements CardEffect {

    public AdjustTimeCountersOnTargetEffect(boolean add) {
        this(add, new Fixed(2));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.anyOf(
                add
                        ? TargetPredicates.permanents(new PermanentHasCountersPredicate(CounterType.TIME))
                        : TargetPredicates.permanent(),
                TargetPredicates.exileCard()));
    }
}
