package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/** Delayed trigger that makes its controller discard a specific card at the next end step. */
public record DiscardSpecificCardAtNextEndStep(UUID controllerId, UUID cardId, Card sourceCard)
        implements DelayedAction {
}
