package com.github.laxika.magicalvibes.model.effect;

/**
 * Target opponent chooses whether the controller draws three cards or mills three cards and the
 * source deals damage to that opponent equal to the total mana value of the cards milled.
 */
public record CombustibleGearhulkEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
