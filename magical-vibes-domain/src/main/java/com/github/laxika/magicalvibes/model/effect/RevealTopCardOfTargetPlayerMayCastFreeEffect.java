package com.github.laxika.magicalvibes.model.effect;

/** Reveals the top card of a target player's library and may cast it for free if it is a nonland. */
public record RevealTopCardOfTargetPlayerMayCastFreeEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
