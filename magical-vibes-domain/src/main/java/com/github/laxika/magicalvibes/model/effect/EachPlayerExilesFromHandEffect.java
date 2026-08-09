package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player exiles {@code amount} cards from their hand (their choice), in APNAP order.
 * Players with fewer than {@code amount} cards exile their entire hand.
 */
public record EachPlayerExilesFromHandEffect(int amount) implements CardEffect {
}
