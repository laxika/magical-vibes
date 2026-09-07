package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;

import java.util.UUID;

/** Delayed trigger that puts the stored counter effect on a permanent at the next end step. */
public record PutCounterOnPermanentAtNextEndStep(
        Card sourceCard, UUID controllerId, UUID permanentId, PutCounterOnTargetPermanentEffect effect)
        implements DelayedAction {

    public PutCounterOnPermanentAtNextEndStep(UUID permanentId, UUID controllerId,
            com.github.laxika.magicalvibes.model.CounterType counterType, int amount, Card sourceCard) {
        this(sourceCard, controllerId, permanentId, new PutCounterOnTargetPermanentEffect(counterType, amount));
    }
}
