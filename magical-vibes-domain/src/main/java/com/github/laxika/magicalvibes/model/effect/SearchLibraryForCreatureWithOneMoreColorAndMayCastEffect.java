package com.github.laxika.magicalvibes.model.effect;

/**
 * Searches the controller's library for a creature card with exactly one more color than the
 * creature sacrificed to pay the same activated ability, exiles it, and offers a normal-cost cast.
 */
public record SearchLibraryForCreatureWithOneMoreColorAndMayCastEffect() implements CardEffect {
}
