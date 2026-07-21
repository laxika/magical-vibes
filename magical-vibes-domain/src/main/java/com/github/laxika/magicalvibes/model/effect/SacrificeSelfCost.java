package com.github.laxika.magicalvibes.model.effect;

/**
 * Sacrifice this permanent as a cost.
 *
 * @param trackPower when true, snapshot this permanent's effective power into the stack entry's
 *                   {@code xValue} at payment time (last-known information after the sacrifice).
 *                   Use with {@code CounterUnlessPaysEffect(0, true, false)} for "unless its
 *                   controller pays {X}, where X is this creature's power" (Mausoleum Wanderer).
 */
public record SacrificeSelfCost(boolean trackPower) implements CostEffect {

    /** Plain "Sacrifice this: …" with no characteristic snapshotting. */
    public SacrificeSelfCost() {
        this(false);
    }

    @Override
    public boolean consumesSourcePermanent() {
        return true;
    }
}
