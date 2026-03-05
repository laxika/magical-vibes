package com.github.laxika.magicalvibes.model.effect;

/**
 * Searches the controller's library for a creature card, reveals it,
 * then shuffles the library and puts that card on top.
 * Used by cards like Brutalizer Exarch.
 */
public record SearchLibraryForCreatureToTopOfLibraryEffect() implements CardEffect {
}
