package com.github.laxika.magicalvibes.model.effect;

/**
 * Turnabout's resolution-time choice to tap or untap all permanents of a chosen permanent type
 * controlled by the target player.
 */
public record TurnaboutEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
