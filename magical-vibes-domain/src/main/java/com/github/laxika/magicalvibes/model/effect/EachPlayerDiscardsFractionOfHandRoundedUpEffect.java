package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player discards 1/divisor of the cards in their hand, rounded up, chosen by that player.
 * The amount is recomputed per player against their own hand size (APNAP order), so each player
 * discards a different number of cards.
 *
 * <p>Example: divisor=3 → each player discards a third of the cards in their hand, rounded up (Pox).
 */
public record EachPlayerDiscardsFractionOfHandRoundedUpEffect(int divisor) implements CardEffect {
}
