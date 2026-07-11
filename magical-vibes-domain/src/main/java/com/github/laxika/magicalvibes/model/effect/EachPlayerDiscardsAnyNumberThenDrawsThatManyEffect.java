package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player discards any number of cards, then draws that many cards.
 * <p>
 * Resolves in APNAP order. Each player in turn chooses how many cards to discard
 * (0 through their hand size) via an X-value choice, discards exactly that many (their
 * own selection), then draws that many cards, before the next player takes their turn.
 * Used by Flux.
 */
public record EachPlayerDiscardsAnyNumberThenDrawsThatManyEffect() implements CardEffect {
}
