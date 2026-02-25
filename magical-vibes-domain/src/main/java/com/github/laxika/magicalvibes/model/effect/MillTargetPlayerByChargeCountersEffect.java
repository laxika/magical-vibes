package com.github.laxika.magicalvibes.model.effect;

/**
 * Target player mills X cards, where X is the number of charge counters on the source permanent.
 * The charge counter count is snapshotted into xValue at activation time.
 */
public record MillTargetPlayerByChargeCountersEffect() implements CardEffect {
    @Override public boolean canTargetPlayer() { return true; }
}
