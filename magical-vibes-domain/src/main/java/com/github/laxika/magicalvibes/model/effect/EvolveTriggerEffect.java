package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Resolution effect for the evolve keyword and evolve-style triggers. The trigger collector stores
 * the entering permanent and its power and toughness at trigger time so resolution can use
 * last-known information if it has left the battlefield.
 *
 * <p>{@code alsoResolve}, when present, is resolved in the same resolution whenever the
 * power/toughness comparison still holds, even if the counter cannot be placed or the source has
 * left the battlefield ("put a +1/+1 counter on this creature and investigate", Sharp-Eyed
 * Rookie). It is distinct from {@code ON_SELF_EVOLVES}, which fires only when the counter lands.</p>
 */
public record EvolveTriggerEffect(CounterType counterType, CardEffect alsoResolve) implements CardEffect {

    public EvolveTriggerEffect(CounterType counterType) {
        this(counterType, null);
    }

    public EvolveTriggerEffect() {
        this(CounterType.PLUS_ONE_PLUS_ONE, null);
    }

    public EvolveTriggerEffect(CardEffect alsoResolve) {
        this(CounterType.PLUS_ONE_PLUS_ONE, alsoResolve);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.NONE;
    }
}
