package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;

import java.util.UUID;

/** A delayed trigger for a qualifying permanent returning from source-linked exile. */
public record DelayedExileReturnCounterTrigger(
        UUID watchedPermanentId,
        UUID controllerId,
        Card sourceCard,
        CounterType counterType,
        int counterAmount) implements DelayedAction {
}
