package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Puts one counter on each of up to {@code maxCount} matching permanents the controller chooses. */
public record PutCounterOnChosenPermanentsEffect(
        CounterType counterType,
        int maxCount,
        PermanentPredicate permanentFilter
) implements CardEffect {

    public PutCounterOnChosenPermanentsEffect {
        if (maxCount < 1) {
            throw new IllegalArgumentException("maxCount must be positive");
        }
    }
}
