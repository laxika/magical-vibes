package com.github.laxika.magicalvibes.model.amount;

/**
 * Evaluates to {@code amount} when the creatures the controller controls have total toughness
 * {@code minTotalToughness} or greater, and to 0 otherwise.
 *
 * <p>A threshold-gated fixed amount used for "this spell costs {N} less to cast if creatures you
 * control have total toughness M or greater" (Orysa, Tide Choreographer). Modeling this as a
 * {@link DynamicAmount} fed straight to {@code ReduceOwnCastCostEffect} — rather than a
 * {@code ConditionalEffect} on the {@code STATIC} slot — keeps the toughness read off the
 * static-bonus computation path, which would otherwise recurse (computing effective toughness
 * re-enters static bonus computation).
 */
public record FixedIfControlledCreaturesTotalToughnessAtLeast(int minTotalToughness, int amount)
        implements DynamicAmount {
}
