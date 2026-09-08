package com.github.laxika.magicalvibes.model.effect;

/** Pays any amount of energy and gives the target creature -1/-1 until end of turn for each energy paid. */
public record PayAnyAmountOfEnergyToBoostTargetCreatureEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
