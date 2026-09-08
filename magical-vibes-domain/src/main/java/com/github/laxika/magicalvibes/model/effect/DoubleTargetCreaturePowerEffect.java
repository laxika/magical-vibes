package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Doubles a target creature's power the evaluated number of times until end of turn.
 * Each doubling uses the creature's current effective power, so repeated doublings compound.
 */
public record DoubleTargetCreaturePowerEffect(DynamicAmount times) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
