package com.github.laxika.magicalvibes.model.effect;

/**
 * Doubles the number of +1/+1 counters on target creature.
 *
 * <p>The effect is targeted and only doubles +1/+1 counters; other counter types remain
 * unchanged.</p>
 */
public record DoublePlusOneCountersOnTargetCreatureEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
