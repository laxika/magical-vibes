package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * Tracks a pending connive action that puts a +1/+1 counter on the source if a nonland card is
 * discarded.
 */
public record PendingConnive(UUID sourcePermanentId) {
}
