package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a card that was in a player's starting deck (the deck the player began the game with), so
 * {@code new CardNotPredicate(new CardInStartingDeckPredicate())} is "a spell that isn't from your starting deck":
 * a conjured, drafted or token card, or one put into the game from outside it. Requires game state.
 */
public record CardInStartingDeckPredicate() implements CardPredicate {
}
