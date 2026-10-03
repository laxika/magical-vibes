package com.github.laxika.magicalvibes.model.effect;

/** Pays any amount of energy and deals that much damage to each other creature. */
public record PayAnyAmountOfEnergyToDealDamageToEachOtherCreatureEffect() implements BoardWipeEffect {

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
