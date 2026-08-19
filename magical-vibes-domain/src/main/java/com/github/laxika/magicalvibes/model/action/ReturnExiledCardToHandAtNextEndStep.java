package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/** Delayed trigger that returns an exiled card to its owner's hand at the next end step. */
public record ReturnExiledCardToHandAtNextEndStep(UUID cardId, UUID ownerId) implements DelayedAction {
}
