package com.github.laxika.magicalvibes.model.effect;

/**
 * Draw N cards then discard N cards (looting).
 * <p>
 * Commonly wrapped in {@link MayEffect} for "you may draw a card. If you do, discard a card."
 *
 * @param drawAmount    number of cards to draw
 * @param discardAmount number of cards to discard
 */
public record DrawAndDiscardCardEffect(int drawAmount, int discardAmount) implements CardEffect {

    public DrawAndDiscardCardEffect() {
        this(1, 1);
    }
}
