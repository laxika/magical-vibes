package com.github.laxika.magicalvibes.model.effect;

/** Replacement behavior for positive energy counters a player would get. */
public interface EnergyCounterReplacementEffect extends CardEffect {

    int replaceEnergy(int count);
}
