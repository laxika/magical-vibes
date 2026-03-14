package com.github.laxika.magicalvibes.model.effect;

/**
 * Reduces this spell's casting cost by {@code amountPerCreature} for each creature on the battlefield
 * (across all players). E.g. Blasphemous Act costs {1} less for each creature on the battlefield.
 */
public record ReduceOwnCastCostPerCreatureOnBattlefieldEffect(int amountPerCreature) implements CardEffect {
}
