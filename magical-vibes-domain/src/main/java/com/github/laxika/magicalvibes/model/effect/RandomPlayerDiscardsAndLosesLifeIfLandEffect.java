package com.github.laxika.magicalvibes.model.effect;

/**
 * Selects a player at random to discard a card; that player loses 3 life if the discarded card is
 * a land. Used by Furnace Layer's planeswalk-to and upkeep triggers.
 */
public record RandomPlayerDiscardsAndLosesLifeIfLandEffect() implements CardEffect {
}
