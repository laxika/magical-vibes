package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player discards all the cards in their hand, then draws that many cards, optionally reduced
 * by a fixed amount.
 * Resolves in APNAP order; each player's draw count equals the number of cards
 * that player discarded minus {@link #drawReduction()}, floored at zero. All discards are
 * automatic (no player choice).
 * Used by Incendiary Command (modal mode) and Dark Deal.
 */
public record EachPlayerDiscardsHandThenDrawsThatManyEffect(int drawReduction) implements CardEffect {

    public EachPlayerDiscardsHandThenDrawsThatManyEffect() {
        this(0);
    }
}
