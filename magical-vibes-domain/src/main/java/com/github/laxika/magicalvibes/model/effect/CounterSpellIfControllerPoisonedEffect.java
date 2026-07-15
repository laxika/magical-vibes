package com.github.laxika.magicalvibes.model.effect;

/**
 * Counters a target spell only if its controller is poisoned (has at least one poison counter).
 * Used by Corrupted Resolve.
 */
public record CounterSpellIfControllerPoisonedEffect() implements CardEffect {
    @Override public TargetSpec targetSpec() { return TargetSpec.benign(TargetCategory.SPELL_ON_STACK); }
}
