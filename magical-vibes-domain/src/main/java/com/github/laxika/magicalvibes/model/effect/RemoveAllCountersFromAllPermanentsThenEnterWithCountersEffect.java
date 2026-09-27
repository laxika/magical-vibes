package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * As this permanent enters, removes all counters from every permanent and makes it enter with
 * that many counters of the configured type.
 */
public record RemoveAllCountersFromAllPermanentsThenEnterWithCountersEffect(CounterType counterType)
        implements ReplacementEffect {
}
