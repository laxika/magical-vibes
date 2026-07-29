package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;

import java.util.UUID;

/**
 * Delayed "at the beginning of your next upkeep, put N {counterType} counters on that permanent"
 * (Cycle of Life). Drained in {@code StepTriggerService.handleUpkeepTriggers} only when
 * {@code controllerId} is the active player; a permanent that has since left the battlefield is
 * silently skipped.
 */
public record PutCounterOnPermanentAtNextUpkeep(UUID controllerId, UUID permanentId,
                                                CounterType counterType, int amount, Card sourceCard)
        implements DelayedAction {
}
