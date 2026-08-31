package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

import java.util.List;

/**
 * Establishes source-independent continuous effects for permanents carrying a counter of the
 * specified type. The effects remain active after the creating permanent leaves and stop applying
 * when the counter is removed.
 */
public record GrantEffectsToCounterBearersEffect(CounterType counterType, List<CardEffect> effects)
        implements CardEffect {

    public GrantEffectsToCounterBearersEffect {
        effects = List.copyOf(effects);
    }
}
