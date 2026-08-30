package com.github.laxika.magicalvibes.model.effect;

/**
 * Sacrifice this permanent as a cost.
 *
 * @param trackPower when true, snapshot this permanent's effective power into the stack entry's
 *                   {@code xValue} at payment time (last-known information after the sacrifice).
 *                   Use with {@code CounterUnlessPaysEffect(0, true, false)} for "unless its
 *                   controller pays {X}, where X is this creature's power" (Mausoleum Wanderer).
 */
public record SacrificeSelfCost(boolean trackPower, boolean recordSacrificedPermanentSnapshot)
        implements CostEffect {

    /** Plain "Sacrifice this: …" with no characteristic snapshotting. */
    public SacrificeSelfCost() {
        this(false, false);
    }

    /** Plain sacrifice cost with optional power snapshotting and no permanent snapshot. */
    public SacrificeSelfCost(boolean trackPower) {
        this(trackPower, false);
    }

    /** Sacrifice cost that preserves the source's last-known counters and attachments. */
    public static SacrificeSelfCost recordingPermanentSnapshot() {
        return new SacrificeSelfCost(false, true);
    }

    @Override
    public boolean consumesSourcePermanent() {
        return true;
    }
}
