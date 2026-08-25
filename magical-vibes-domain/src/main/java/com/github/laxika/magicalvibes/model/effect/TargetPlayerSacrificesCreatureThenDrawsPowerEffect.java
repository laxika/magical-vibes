package com.github.laxika.magicalvibes.model.effect;

/**
 * "Target player sacrifices a creature of their choice. You draw cards equal to that creature's
 * power."
 *
 * <p>The creature is chosen at resolution and its effective power is captured before it leaves the
 * battlefield. The target player sacrifices the creature, while the effect controller draws the
 * cards.
 */
public record TargetPlayerSacrificesCreatureThenDrawsPowerEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
