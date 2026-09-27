package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/** Each player may choose one creature they control and put the specified counters on it. */
public record EachPlayerMayPutCountersOnCreatureEffect(
        CounterType counterType,
        int count,
        CardEffect acceptedFollowUp
) implements CardEffect {

    public EachPlayerMayPutCountersOnCreatureEffect(CounterType counterType, int count) {
        this(counterType, count, null);
    }
}
