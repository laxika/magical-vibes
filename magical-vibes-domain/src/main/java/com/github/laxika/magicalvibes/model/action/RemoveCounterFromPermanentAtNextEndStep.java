package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;

import java.util.UUID;

/** Delayed trigger that removes one counter from a creature at the beginning of the next end step. */
public record RemoveCounterFromPermanentAtNextEndStep(
        Card sourceCard, UUID controllerId, UUID permanentId, CounterType counterType)
        implements DelayedAction {
}
