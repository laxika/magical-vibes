package com.github.laxika.magicalvibes.model.effect;

/**
 * Sets a target creature's base power and toughness to the source permanent's current effective
 * power and toughness until end of turn.
 */
public record SetTargetBasePowerToughnessFromSourceEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
