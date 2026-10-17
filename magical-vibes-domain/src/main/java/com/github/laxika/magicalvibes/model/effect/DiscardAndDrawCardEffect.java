package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

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
 * @param random        whether the discarded cards are chosen at random
 * @param drawRegardless true for "discard N, then draw N" (no "if you do"): the draw still happens
 *                       when the hand is empty and nothing could be discarded
 */
public record DiscardAndDrawCardEffect(int discardAmount, int drawAmount, boolean random, boolean drawRegardless)
        implements CardDrawingEffect {

    public DiscardAndDrawCardEffect(int discardAmount, int drawAmount, boolean random) {
        this(discardAmount, drawAmount, random, false);
    }

    public DiscardAndDrawCardEffect(int discardAmount, int drawAmount) {
        this(discardAmount, drawAmount, false);
    }

    public DiscardAndDrawCardEffect() {
        this(1, 1, false);
    }

    /** "Discard N cards, then draw N cards." — the draw is not contingent on the discard. */
    public static DiscardAndDrawCardEffect thenDraw(int discardAmount, int drawAmount) {
        return new DiscardAndDrawCardEffect(discardAmount, drawAmount, false, true);
    }

    @Override
    public DynamicAmount drawnCardAmount() {
        return new Fixed(drawAmount);
    }
}
