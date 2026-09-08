package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

import java.util.List;

/**
 * Removes one counter of the given type from the source permanent, or sacrifices it when no such
 * counter remains. Used for upkeep abilities such as fading.
 */
public record RemoveCounterOrSacrificeSelfEffect(CounterType counterType, List<CardEffect> thenEffects)
        implements CardEffect {

    public RemoveCounterOrSacrificeSelfEffect(CounterType counterType) {
        this(counterType, List.of());
    }

    public RemoveCounterOrSacrificeSelfEffect(CounterType counterType, CardEffect thenEffect) {
        this(counterType, List.of(thenEffect));
    }

    public RemoveCounterOrSacrificeSelfEffect {
        thenEffects = List.copyOf(thenEffects);
    }

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(null, false, null, true, 1);
    }
}
