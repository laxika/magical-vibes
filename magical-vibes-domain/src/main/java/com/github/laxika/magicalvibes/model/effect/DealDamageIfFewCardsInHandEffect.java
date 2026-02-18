package com.github.laxika.magicalvibes.model.effect;

/**
 * Deals damage to the target player if they have {@code maxCards} or fewer cards in hand.
 * Used for intervening-if triggered abilities like Lavaborn Muse.
 * The condition is checked both at trigger time and at resolution time.
 */
public record DealDamageIfFewCardsInHandEffect(int maxCards, int damage) implements CardEffect {
}
