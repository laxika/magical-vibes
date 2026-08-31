package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;

import java.util.UUID;

/** Delayed trigger that removes counters from a permanent at the beginning of the next cleanup step. */
public record RemoveCountersFromPermanentAtNextCleanup(
        Card sourceCard, UUID controllerId, UUID permanentId, CounterType counterType, int amount)
        implements DelayedAction {
}
