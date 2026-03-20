package com.github.laxika.magicalvibes.model.effect;

/**
 * Searches the controller's library for two cards (unrestricted). The controller
 * puts one into their hand and the other into their graveyard, then shuffles.
 * Used by Final Parting.
 */
public record SearchLibraryForCardToHandAndCardToGraveyardEffect() implements CardEffect {
}
