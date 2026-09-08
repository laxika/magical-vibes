package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * Tracks the remaining discards in a connive action and the permanent receiving counters for
 * nonland cards.
 */
public record PendingConnive(UUID sourcePermanentId, int remainingDiscards) {

    public PendingConnive(UUID sourcePermanentId) {
        this(sourcePermanentId, 1);
    }
}
