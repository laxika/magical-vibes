package com.github.laxika.magicalvibes.model.effect;

/** Suspects the damaged creature and perpetually grants it this same combat-damage ability. */
public record SuspectAndPerpetuallyGrantAbilityToTargetCreatureEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
