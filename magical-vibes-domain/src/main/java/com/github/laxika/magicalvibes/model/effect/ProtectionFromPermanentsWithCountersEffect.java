package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

import java.util.Set;

/** Grants protection from permanents carrying a counter of the specified type. */
public record ProtectionFromPermanentsWithCountersEffect(CounterType counterType)
        implements ProtectionGrantingEffect {

    @Override
    public Set<CounterType> protectionFromPermanentsWithCounters() {
        return Set.of(counterType);
    }
}
