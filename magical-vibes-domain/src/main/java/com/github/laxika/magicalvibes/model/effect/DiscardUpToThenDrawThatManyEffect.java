package com.github.laxika.magicalvibes.model.effect;

/**
 * Discard up to N cards (or any number when {@link #ANY_NUMBER}), then draw that many cards
 * plus {@code extraDraw} (rummaging with "up to" or "any number" choice).
 * <p>
 * Resolution is a two-phase interaction:
 * <ol>
 *   <li>Ask the player to choose how many cards to discard (0 to min(maxDiscard, hand size),
 *       or 0 to hand size when maxDiscard is {@link #ANY_NUMBER})</li>
 *   <li>Discard that many cards, then draw that many cards plus {@code extraDraw}</li>
 * </ol>
 *
 * @param maxDiscard the maximum number of cards the player may discard, or {@link #ANY_NUMBER}
 * @param extraDraw additional cards drawn beyond the number discarded (e.g. 1 for "plus one")
 */
public record DiscardUpToThenDrawThatManyEffect(int maxDiscard, int extraDraw) implements CardEffect {

    /** Sentinel for "discard any number of cards" (0 through current hand size). */
    public static final int ANY_NUMBER = -1;

    public DiscardUpToThenDrawThatManyEffect(int maxDiscard) {
        this(maxDiscard, 0);
    }
}
