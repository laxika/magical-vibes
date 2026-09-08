package com.github.laxika.magicalvibes.model.effect;

/**
 * Counters a target spell only if its controller has at least the configured number of poison
 * counters. Used by Corrupted Resolve and Bring the Ending.
 */
public record CounterSpellIfControllerPoisonedEffect(int minimumPoisonCounters) implements CardEffect {
    public CounterSpellIfControllerPoisonedEffect() {
        this(1);
    }

    @Override public TargetSpec targetSpec() { return TargetSpec.benign(TargetPredicates.spellOnStack()); }
}
