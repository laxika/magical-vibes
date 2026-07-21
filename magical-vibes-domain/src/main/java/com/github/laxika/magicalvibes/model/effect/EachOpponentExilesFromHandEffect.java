package com.github.laxika.magicalvibes.model.effect;

/**
 * Each opponent exiles {@code amount} cards from their hand (their choice). Non-targeting.
 * Opponents with fewer than {@code amount} cards exile their entire hand. Uses the shared
 * {@code EXILE_FROM_HAND_CHOICE} interaction (Nicol Bolas, God-Pharaoh +1).
 */
public record EachOpponentExilesFromHandEffect(int amount) implements CardEffect {
}
