package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Puts counters on one matching permanent chosen during resolution, regardless of controller.
 */
public record PutCounterOnChosenPermanentEffect(CounterType counterType, DynamicAmount amount,
                                                 PermanentPredicate predicate) implements CardEffect {

    public PutCounterOnChosenPermanentEffect(CounterType counterType, int count,
                                               PermanentPredicate predicate) {
        this(counterType, new Fixed(count), predicate);
    }
}
