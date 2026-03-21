package com.github.laxika.magicalvibes.model.effect;

/**
 * Discard up to N cards, then draw that many cards (rummaging with "up to" choice).
 * <p>
 * Resolution is a two-phase interaction:
 * <ol>
 *   <li>Ask the player to choose how many cards to discard (0 to min(maxDiscard, hand size))</li>
 *   <li>Discard that many cards, then draw that many cards</li>
 * </ol>
 *
 * @param maxDiscard the maximum number of cards the player may discard
 */
public record DiscardUpToThenDrawThatManyEffect(int maxDiscard) implements CardEffect {
}
