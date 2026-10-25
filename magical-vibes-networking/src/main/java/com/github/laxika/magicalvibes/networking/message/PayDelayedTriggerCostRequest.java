package com.github.laxika.magicalvibes.networking.message;

import java.util.UUID;

/**
 * Special action: pay the cost of the delayed ability of {@code sourceCardId} that would affect
 * {@code affectedId} ("unless that player pays [cost] before that step"), so it won't trigger.
 */
public record PayDelayedTriggerCostRequest(UUID sourceCardId, UUID affectedId) {
}
