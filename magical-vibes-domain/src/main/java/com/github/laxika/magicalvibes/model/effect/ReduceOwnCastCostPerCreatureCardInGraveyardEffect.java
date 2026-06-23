package com.github.laxika.magicalvibes.model.effect;

/**
 * Reduces this spell's casting cost by {@code amountPerCreature} for each creature card in the
 * controller's graveyard. E.g. Ghoultree costs {1} less to cast for each creature card in your graveyard.
 */
public record ReduceOwnCastCostPerCreatureCardInGraveyardEffect(int amountPerCreature) implements CardEffect {
}
