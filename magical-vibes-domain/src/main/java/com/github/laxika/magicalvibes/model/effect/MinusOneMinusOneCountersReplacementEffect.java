package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/** Replacement behavior for -1/-1 counters put on creatures. */
public interface MinusOneMinusOneCountersReplacementEffect extends CounterReplacementEffect {

    int replace(int count);

    @Override
    default int replace(CounterType counterType, int count) {
        return replace(count);
    }

    @Override
    default boolean appliesTo(CounterType counterType, boolean affectedPermanentIsCreature) {
        return affectedPermanentIsCreature && counterType == CounterType.MINUS_ONE_MINUS_ONE;
    }
}
