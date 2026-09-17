package com.github.laxika.magicalvibes.model.effect;

/** Makes the target player play with their hand revealed for the rest of the game. */
public record PlayWithTargetPlayerHandRevealedEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
