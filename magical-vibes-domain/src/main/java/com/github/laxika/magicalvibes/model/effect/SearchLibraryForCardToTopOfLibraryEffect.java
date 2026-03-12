package com.github.laxika.magicalvibes.model.effect;

/**
 * Searches the controller's library for any card,
 * then shuffles the library and puts that card on top.
 * Used by cards like Liliana Vess.
 */
public record SearchLibraryForCardToTopOfLibraryEffect() implements CardEffect {
}
