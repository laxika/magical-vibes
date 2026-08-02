package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Death trigger for "When this creature dies, create N of the given token for each counter on it"
 * (e.g. Kinsbaile Borderguard: a 1/1 white Kithkin Soldier for each counter on it).
 * <p>
 * Placed on the {@code ON_DEATH} slot. The death-trigger collector snapshots the dying permanent's
 * counter count and resolves into a copy of {@code tokenTemplate} whose amount is that count,
 * reusing the standard token-creation handler. A non-null {@code counterType} limits the count to
 * that counter type; null preserves the original all-counter behavior.
 *
 * @param counterType the counter type to count, or null for every concrete counter type
 * @param tokenTemplate the token to create once per counter on the dying creature
 */
public record CreateTokensForEachDyingSourceCounterEffect(
        CounterType counterType,
        CreateTokenEffect tokenTemplate
) implements CardEffect {

    public CreateTokensForEachDyingSourceCounterEffect(CreateTokenEffect tokenTemplate) {
        this(null, tokenTemplate);
    }
}
