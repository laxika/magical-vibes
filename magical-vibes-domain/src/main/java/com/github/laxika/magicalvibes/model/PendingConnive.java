package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * Tracks the remaining discards in a connive action and the permanent receiving counters for
 * nonland cards.
 *
 * <p>{@code lastKnownSource} and {@code lastKnownControllerId} carry the conniving permanent's last
 * known information when it was no longer on the battlefield as it was instructed to connive; it
 * still connives for "whenever a creature connives" triggers but receives no counter.
 *
 * <p>{@code nonlandDiscards} counts the nonland cards discarded so far: connive N puts that many
 * +1/+1 counters on the permanent in a single placement after all N discards (CR 701.50d).
 */
public record PendingConnive(UUID sourcePermanentId, int remainingDiscards, Permanent lastKnownSource,
                             UUID lastKnownControllerId, int nonlandDiscards) {

    public PendingConnive(UUID sourcePermanentId, int remainingDiscards, Permanent lastKnownSource,
                          UUID lastKnownControllerId) {
        this(sourcePermanentId, remainingDiscards, lastKnownSource, lastKnownControllerId, 0);
    }

    public PendingConnive(UUID sourcePermanentId, int remainingDiscards) {
        this(sourcePermanentId, remainingDiscards, null, null);
    }

    public PendingConnive(UUID sourcePermanentId) {
        this(sourcePermanentId, 1);
    }

    public PendingConnive withRemainingDiscards(int remaining) {
        return new PendingConnive(sourcePermanentId, remaining, lastKnownSource, lastKnownControllerId,
                nonlandDiscards);
    }

    /** Records one more nonland discard; the counters are put on the permanent once, after the last discard. */
    public PendingConnive withNonlandDiscards(int nonland) {
        return new PendingConnive(sourcePermanentId, remainingDiscards, lastKnownSource, lastKnownControllerId,
                nonland);
    }
}
