package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * Tracks the remaining discards in a connive action and the permanent receiving counters for
 * nonland cards.
 *
 * <p>{@code lastKnownSource} and {@code lastKnownControllerId} carry the conniving permanent's last
 * known information when it was no longer on the battlefield as it was instructed to connive; it
 * still connives for "whenever a creature connives" triggers but receives no counter.
 */
public record PendingConnive(UUID sourcePermanentId, int remainingDiscards, Permanent lastKnownSource,
                             UUID lastKnownControllerId) {

    public PendingConnive(UUID sourcePermanentId, int remainingDiscards) {
        this(sourcePermanentId, remainingDiscards, null, null);
    }

    public PendingConnive(UUID sourcePermanentId) {
        this(sourcePermanentId, 1);
    }

    public PendingConnive withRemainingDiscards(int remaining) {
        return new PendingConnive(sourcePermanentId, remaining, lastKnownSource, lastKnownControllerId);
    }
}
