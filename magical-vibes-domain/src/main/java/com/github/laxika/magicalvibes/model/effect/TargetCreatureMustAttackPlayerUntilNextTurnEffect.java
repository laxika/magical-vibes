package com.github.laxika.magicalvibes.model.effect;

/**
 * Gives a targeted creature a temporary requirement to attack a player each combat if able.
 */
public record TargetCreatureMustAttackPlayerUntilNextTurnEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
