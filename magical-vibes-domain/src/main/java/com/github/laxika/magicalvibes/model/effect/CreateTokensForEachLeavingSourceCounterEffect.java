package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

import java.util.Map;

/**
 * Leave-the-battlefield trigger that creates one token for each counter of the named type on the
 * departing permanent.
 */
public record CreateTokensForEachLeavingSourceCounterEffect(
        CounterType counterType,
        CreateTokenEffect tokenTemplate
) implements CardEffect, LeavingPermanentCountersAwareEffect {

    @Override
    public CardEffect boundToLeavingPermanentCounters(Map<CounterType, Integer> counters) {
        int count = counterType == null
                ? counters.values().stream().mapToInt(Integer::intValue).sum()
                : counters.getOrDefault(counterType, 0);
        return count > 0 ? tokenTemplate.withAmount(count) : null;
    }
}
