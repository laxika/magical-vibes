package com.github.laxika.magicalvibes.model.effect;

/**
 * Reduces the controller's generic spell costs by one for each non-token card with the same name
 * as the spell in that player's graveyard.
 */
public record ReduceCastCostForSameNameCardsInGraveyardEffect() implements CardEffect {
}
