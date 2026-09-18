package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/** Delayed trigger that returns an uncast exiled card to its owner's hand at the next end step. */
public record ReturnExiledCardToHandAtNextEndStep(UUID cardId, UUID ownerId, Card sourceCard, UUID controllerId)
        implements DelayedAction {
    public ReturnExiledCardToHandAtNextEndStep(UUID cardId, UUID ownerId) {
        this(cardId, ownerId, null, ownerId);
    }

    public ReturnExiledCardToHandAtNextEndStep(UUID cardId, UUID ownerId, Card sourceCard) {
        this(cardId, ownerId, sourceCard, ownerId);
    }
}
