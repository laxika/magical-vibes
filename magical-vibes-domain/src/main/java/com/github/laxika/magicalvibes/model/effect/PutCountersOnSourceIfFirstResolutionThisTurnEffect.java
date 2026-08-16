package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Puts counters on the source permanent only when this keyed ability resolves for the first time
 * during the current turn.
 *
 * <p>The key distinguishes separate abilities on the same permanent. The resolution is recorded
 * before attempting to find the source, so a resolved ability still counts if the source has left
 * the battlefield in response.</p>
 */
public record PutCountersOnSourceIfFirstResolutionThisTurnEffect(
        String abilityKey,
        CounterType counterType,
        int count
) implements CardEffect {

    public PutCountersOnSourceIfFirstResolutionThisTurnEffect(CounterType counterType, int count) {
        this("default", counterType, count);
    }
}
