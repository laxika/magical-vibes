package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;

import java.util.UUID;

/**
 * Delayed trigger that removes up to {@code count} counters of a type from a permanent at the beginning
 * of the next end step, as one triggered ability.
 */
public record RemoveCounterFromPermanentAtNextEndStep(
        Card sourceCard, UUID controllerId, UUID permanentId, CounterType counterType, int count)
        implements DelayedAction {
}
