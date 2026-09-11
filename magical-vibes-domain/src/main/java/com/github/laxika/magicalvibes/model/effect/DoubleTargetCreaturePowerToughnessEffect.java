package com.github.laxika.magicalvibes.model.effect;

/** Doubles a target creature's power and toughness until end of turn. */
public record DoubleTargetCreaturePowerToughnessEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
