package com.github.laxika.magicalvibes.model.effect;

/**
 * "For each creature token you control, create a token that's a copy of that creature."
 *
 * <p>The set of creature tokens is snapshotted before any copies are made (CR 706.10 style
 * populate-all), so the newly created copies are not themselves copied. Each snapshotted token is
 * copied via all copiable characteristics per CR 707.2, respecting the controller's token
 * multiplier (Doubling Season). Used by Rhys the Redeemed's second ability.
 */
public record CreateTokenCopyOfEachControlledCreatureTokenEffect() implements CardEffect {
}
