package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;

import java.util.UUID;

/** Delayed triggered ability that puts counters on a remembered permanent at the next end step. */
public record PutCounterOnPermanentAtNextEndStep(
        UUID permanentId,
        UUID controllerId,
        CounterType counterType,
        int amount,
        Card sourceCard
) implements DelayedAction {
}
