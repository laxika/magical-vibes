package com.github.laxika.magicalvibes.model.effect;

/**
 * Discard N cards then draw N cards (rummaging).
 * <p>
 * Commonly wrapped in {@link MayEffect} for "you may discard a card. If you do, draw a card."
 * <p>
 * Unlike looting ("draw N, then discard M" — composed as
 * {@code MayEffect(SequenceEffect.of(new DrawCardEffect(n), new DiscardEffect(m, CONTROLLER)))}),
 * this effect discards first, then draws, and the draw only happens after the player has actually
 * discarded. That "if you do" contingency between the two steps is why rummaging cannot be folded
 * into a plain {@link SequenceEffect} (which resolves its steps with no data flow between them).
 *
 * @param discardAmount number of cards to discard
 * @param drawAmount    number of cards to draw
 */
public record DiscardAndDrawCardEffect(int discardAmount, int drawAmount) implements CardEffect {

    public DiscardAndDrawCardEffect() {
        this(1, 1);
    }
}
