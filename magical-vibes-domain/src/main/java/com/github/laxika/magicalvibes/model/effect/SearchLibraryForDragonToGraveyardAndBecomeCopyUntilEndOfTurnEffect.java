package com.github.laxika.magicalvibes.model.effect;

/**
 * Searches the controller's library for a Dragon permanent card and puts the chosen card into
 * their graveyard. The library-search continuation then makes the source a copy of that card.
 */
public record SearchLibraryForDragonToGraveyardAndBecomeCopyUntilEndOfTurnEffect() implements CardEffect {
}
