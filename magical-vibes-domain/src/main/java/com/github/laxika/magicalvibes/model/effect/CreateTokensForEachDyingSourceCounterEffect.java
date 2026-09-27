package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

import java.util.Map;

/**
 * Death trigger for "When this creature dies, create N of the given token for each counter on it"
 * (e.g. Kinsbaile Borderguard: a 1/1 white Kithkin Soldier for each counter on it).
 * <p>
 * Placed on the {@code ON_DEATH} or {@code ON_ANY_CREATURE_DIES} slot. It can also be used on
 * {@code ON_ALLY_CREATURE_DIES}; that collector binds the dying permanent's counter snapshot before
 * the effect is put on the stack.
 * The death-trigger collector
 * snapshots the dying permanent's counter count and resolves into a copy of {@code tokenTemplate}
 * whose amount is that count, reusing the standard token-creation handler. A non-null
 * {@code counterType} limits the count to that counter type; null preserves the original all-counter
 * behavior.
 *
 * @param counterType the counter type to count, or null for every concrete counter type
 * @param tokenTemplate the token to create once per counter on the dying creature
 */
public record CreateTokensForEachDyingSourceCounterEffect(
        CounterType counterType,
        CreateTokenEffect tokenTemplate
) implements CardEffect, DyingCreatureCountersAwareEffect {

    public CreateTokensForEachDyingSourceCounterEffect(CreateTokenEffect tokenTemplate) {
        this(null, tokenTemplate);
    }

    @Override
    public CardEffect boundToDyingCreatureCounters(Map<CounterType, Integer> counters) {
        int count = counterType == null
                ? counters.values().stream().mapToInt(Integer::intValue).sum()
                : counters.getOrDefault(counterType, 0);
        return tokenTemplate.withAmount(count);
    }
}
