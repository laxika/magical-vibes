package com.github.laxika.magicalvibes.model.effect;

/**
 * "Choose a creature type. Target player draws X cards and loses X life, where X is the number
 * of creatures they control of the chosen type."
 */
public record TargetPlayerDrawsAndLosesLifePerChosenTypeEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
