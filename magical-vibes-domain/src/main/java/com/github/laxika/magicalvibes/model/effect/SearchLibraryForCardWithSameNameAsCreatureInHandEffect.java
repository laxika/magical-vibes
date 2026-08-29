package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveal a creature card from the controller's hand, then search that library for a card with the
 * revealed card's name, reveal it, put it into hand, and shuffle.
 *
 * <p>The creature card is chosen during resolution and remains in its controller's hand.</p>
 */
public record SearchLibraryForCardWithSameNameAsCreatureInHandEffect() implements CardEffect {
}
