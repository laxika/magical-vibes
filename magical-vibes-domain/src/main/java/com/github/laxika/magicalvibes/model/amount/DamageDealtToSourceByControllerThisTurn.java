package com.github.laxika.magicalvibes.model.amount;

/**
 * The damage dealt to the source permanent this turn by sources controlled by the player
 * evaluating the amount. Prevented damage is not included.
 */
public record DamageDealtToSourceByControllerThisTurn() implements DynamicAmount {
}
