package com.github.laxika.magicalvibes.model.effect;

/**
 * Caradora, Heart of Alacria: adds one +1/+1 counter to counters put on a creature or Vehicle
 * controlled by the effect's controller.
 */
public record AddOnePlusOneCountersToCreaturesAndVehiclesEffect()
        implements PlusOnePlusOneCountersReplacementEffect {

    @Override
    public int replace(int count) {
        return count > 0 ? count + 1 : count;
    }

    @Override
    public boolean appliesToNonCreatureVehicles() {
        return true;
    }
}
