package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Resolution effect for the evolve keyword. The trigger collector stores the entering permanent
 * and its power and toughness at trigger time so resolution can use last-known information if it
 * has left the battlefield.
 */
public record EvolveTriggerEffect(CounterType counterType) implements CardEffect {

    public EvolveTriggerEffect() {
        this(CounterType.PLUS_ONE_PLUS_ONE);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.NONE;
    }
}
