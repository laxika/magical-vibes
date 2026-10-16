package com.github.laxika.magicalvibes.model.effect;

/**
 * Sacrifice this permanent as a cost.
 *
 * @param trackPower when true, snapshot this permanent's effective power into the stack entry's
 *                   {@code xValue} at payment time (last-known information after the sacrifice).
 *                   Use with {@code CounterUnlessPaysEffect(0, true, false)} for "unless its
 *                   controller pays {X}, where X is this creature's power" (Mausoleum Wanderer).
 * @param signedPower when true together with {@code trackPower}, a negative power is kept as a
 *                   negative X instead of being treated as 0, for comparisons against the power
 *                   rather than amounts (Lena, Selfless Champion).
 */
public record SacrificeSelfCost(boolean trackPower, boolean recordSacrificedPermanentSnapshot, boolean signedPower)
        implements CostEffect {

    public SacrificeSelfCost(boolean trackPower, boolean recordSacrificedPermanentSnapshot) {
        this(trackPower, recordSacrificedPermanentSnapshot, false);
    }

    /** Plain "Sacrifice this: …" with no characteristic snapshotting. */
    public SacrificeSelfCost() {
        this(false, false);
    }

    /** Plain sacrifice cost with optional power snapshotting and no permanent snapshot. */
    public SacrificeSelfCost(boolean trackPower) {
        this(trackPower, false);
    }

    /** Sacrifice cost that snapshots the source's power as a signed X (negative power stays negative). */
    public static SacrificeSelfCost trackingSignedPower() {
        return new SacrificeSelfCost(true, false, true);
    }

    /** Sacrifice cost that preserves the source's last-known counters and attachments. */
    public static SacrificeSelfCost recordingPermanentSnapshot() {
        return new SacrificeSelfCost(false, true);
    }

    @Override
    public boolean consumesSourcePermanent() {
        return true;
    }

    @Override
    public boolean sacrificesSourcePermanent() {
        return true;
    }
}
