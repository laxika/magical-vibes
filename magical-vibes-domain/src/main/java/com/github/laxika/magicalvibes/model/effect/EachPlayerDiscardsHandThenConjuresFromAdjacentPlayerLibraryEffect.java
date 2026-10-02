package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes each player discard their hand, then conjure a fixed number of random library-card
 * duplicates from the player in the specified seating direction into that player's hand.
 */
public record EachPlayerDiscardsHandThenConjuresFromAdjacentPlayerLibraryEffect(
        int count, PlayerDirection direction) implements CardEffect {
}
