package com.github.laxika.magicalvibes.model.effect;

/**
 * Target player exiles every card from their hand face down, tracks those cards with the source
 * permanent, then draws the same number of cards.
 */
public record ExileTargetPlayerHandFaceDownWithSourceThenDrawEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
