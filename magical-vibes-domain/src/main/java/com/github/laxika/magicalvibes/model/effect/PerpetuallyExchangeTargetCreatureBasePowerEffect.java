package com.github.laxika.magicalvibes.model.effect;

/** Perpetually exchanges the base power of two target creatures. */
public record PerpetuallyExchangeTargetCreatureBasePowerEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
