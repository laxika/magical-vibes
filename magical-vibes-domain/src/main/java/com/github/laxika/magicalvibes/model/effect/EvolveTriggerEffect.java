package com.github.laxika.magicalvibes.model.effect;

/**
 * Resolution effect for the evolve keyword. The trigger collector stores the entering permanent
 * and its power and toughness at trigger time so resolution can use last-known information if it
 * has left the battlefield.
 */
public record EvolveTriggerEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.NONE;
    }
}
