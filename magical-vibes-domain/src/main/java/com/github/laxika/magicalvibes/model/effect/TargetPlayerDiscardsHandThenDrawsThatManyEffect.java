package com.github.laxika.magicalvibes.model.effect;

/**
 * Target player discards all the cards in their hand, then draws that many cards.
 * All discards are automatic (no player choice). Fires discard triggers for each card.
 * Used by Collective Defiance (modal mode).
 */
public record TargetPlayerDiscardsHandThenDrawsThatManyEffect() implements CardEffect {
    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PLAYER);
    }
}
