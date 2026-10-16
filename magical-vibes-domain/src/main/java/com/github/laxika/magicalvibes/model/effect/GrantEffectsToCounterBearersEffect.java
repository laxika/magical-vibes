package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

import java.util.List;

/**
 * Establishes source-independent continuous effects for permanents carrying a counter of the
 * specified type. The effects remain active after the creating permanent leaves and stop applying
 * when the counter is removed.
 *
 * <p>When {@code boundToTargetCard} is set, the effects apply only to the permanent that the
 * spell's targeted card became on the battlefield ("for as long as that creature has a duty counter
 * on it"), and they end for good once that permanent no longer has the counter.
 */
public record GrantEffectsToCounterBearersEffect(CounterType counterType, List<CardEffect> effects,
                                                 boolean boundToTargetCard)
        implements CardEffect {

    public GrantEffectsToCounterBearersEffect {
        effects = List.copyOf(effects);
    }

    public GrantEffectsToCounterBearersEffect(CounterType counterType, List<CardEffect> effects) {
        this(counterType, effects, false);
    }
}
