package com.github.laxika.magicalvibes.model.effect;

/**
 * Deals damage to each player equal to 1/divisor of that player's life total, rounded down.
 * Amounts are snapshotted from current life totals before any damage is applied (simultaneous).
 * Example: divisor=2 → half that player's life, rounded down (Heartless Hidetsugu).
 *
 * <p>Per-player amounts cannot use {@link DealDamageToPlayersEffect} (once-evaluated amount).
 * Life-loss sibling: {@link EachPlayerLosesFractionOfLifeRoundedUpEffect}.
 */
public record DealDamageToEachPlayerEqualToFractionOfLifeRoundedDownEffect(int divisor) implements CardEffect {
}
