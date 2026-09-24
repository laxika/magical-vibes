package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Returns the permanent recorded as sacrificed on the resolving stack entry from its owner's
 * graveyard to the battlefield, with counters placed as it enters.
 */
public record ReturnSacrificedPermanentToBattlefieldEffect(CounterType counterType, int counterAmount)
        implements CardEffect {

    public ReturnSacrificedPermanentToBattlefieldEffect {
        if (counterType == null || counterAmount <= 0) {
            throw new IllegalArgumentException("A positive counter amount and counter type are required");
        }
    }
}
