package com.github.laxika.magicalvibes.model.amount;

/**
 * The total damage actually dealt to the targeted permanent this turn. Prevented damage is not
 * included, and the total survives effects that remove damage marked on the permanent, such as
 * regeneration.
 */
public record DamageDealtToTargetPermanentThisTurn() implements DynamicAmount {
}
