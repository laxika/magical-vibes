package com.github.laxika.magicalvibes.model.effect;

/**
 * Discard N cards then draw N cards (rummaging).
 * <p>
 * Commonly wrapped in {@link MayEffect} for "you may discard a card. If you do, draw a card."
 * <p>
 * Unlike {@link DrawAndDiscardCardEffect} (looting), this effect discards first, then draws.
 * The draw only happens after the player has actually discarded.
 *
 * @param discardAmount number of cards to discard
 * @param drawAmount    number of cards to draw
 */
public record DiscardAndDrawCardEffect(int discardAmount, int drawAmount) implements CardEffect {

    public DiscardAndDrawCardEffect() {
        this(1, 1);
    }
}
