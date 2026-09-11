package com.github.laxika.magicalvibes.model.effect;

/**
 * Sets the source permanent's base power to the target creature's current power indefinitely.
 */
public record SetSelfBasePowerFromTargetPowerEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
