package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player discards cards from their hand down to the number of cards held by the player with the
 * fewest cards in hand. Each player chooses which of their own cards to discard (APNAP order).
 *
 * <p>Example: the discard step of Balance — every player discards down to the smallest hand size.
 */
public record EachPlayerDiscardsDownToFewestEffect() implements CardEffect {
}
