package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/** Kami of Whispered Hopes-style replacement for +1/+1 counters put on permanents. */
public record AddOnePlusOneCountersToPermanentsEffect()
        implements PlusOnePlusOneCountersReplacementEffect {

    @Override
    public int replace(int count) {
        return count > 0 ? count + 1 : count;
    }

    @Override
    public boolean appliesTo(CounterType counterType, boolean affectedPermanentIsCreature) {
        return counterType == CounterType.PLUS_ONE_PLUS_ONE;
    }
}
