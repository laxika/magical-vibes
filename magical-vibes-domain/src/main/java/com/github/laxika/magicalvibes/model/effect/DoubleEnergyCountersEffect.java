package com.github.laxika.magicalvibes.model.effect;

/** Static replacement effect that doubles positive energy-counter gains. */
public record DoubleEnergyCountersEffect() implements EnergyCounterReplacementEffect, DoublingEffect {

    @Override
    public int replaceEnergy(int count) {
        return count > 0 ? count * 2 : count;
    }
}
