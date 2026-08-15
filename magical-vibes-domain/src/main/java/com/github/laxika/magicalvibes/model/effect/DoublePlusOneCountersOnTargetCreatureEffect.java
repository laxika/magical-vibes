package com.github.laxika.magicalvibes.model.effect;

/**
 * Doubles the number of +1/+1 counters on each target creature.
 */
public record DoublePlusOneCountersOnTargetCreatureEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
