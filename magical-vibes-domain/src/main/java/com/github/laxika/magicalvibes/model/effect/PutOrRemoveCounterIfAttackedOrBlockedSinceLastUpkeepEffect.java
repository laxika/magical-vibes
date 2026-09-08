package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * At upkeep, puts a counter on the referenced permanent if it attacked, blocked, or was blocked
 * since the relevant last upkeep; otherwise removes one counter of that type.
 */
public record PutOrRemoveCounterIfAttackedOrBlockedSinceLastUpkeepEffect(
        PermanentReference reference,
        CounterType counterType
) implements CardEffect {

    public PutOrRemoveCounterIfAttackedOrBlockedSinceLastUpkeepEffect(CounterType counterType) {
        this(PermanentReference.SOURCE, counterType);
    }
}
