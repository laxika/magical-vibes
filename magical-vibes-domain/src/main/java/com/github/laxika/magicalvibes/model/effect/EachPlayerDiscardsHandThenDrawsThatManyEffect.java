package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player discards all the cards in their hand, then draws that many cards.
 * Resolves in APNAP order; each player's draw count equals the number of cards
 * that player discarded. All discards are automatic (no player choice).
 * Used by Incendiary Command (modal mode).
 */
public record EachPlayerDiscardsHandThenDrawsThatManyEffect() implements CardEffect {
}
