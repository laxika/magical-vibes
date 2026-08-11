package com.github.laxika.magicalvibes.model.effect;

/**
 * Choose another creature you control, then search your library for a card with that creature's
 * name, reveal it, put it into your hand, and shuffle.
 *
 * <p>The creature is chosen during resolution and is not a target.
 */
public record SearchLibraryForCardWithSameNameAsAnotherCreatureYouControlEffect() implements CardEffect {
}
