package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes the target player play with their hand revealed for the rest of the game: it resolves into itself as a
 * {@link Duration#PERMANENT} floating effect on that player (CR 611.2a).
 */
public record PlayWithTargetPlayerHandRevealedEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
