package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/** Marks the Chapter III choice for returning two cards exiled with a Saga. */
public record PendingReturnTwoExiledWithSourceCards(UUID controllerId, UUID sourcePermanentId)
        implements PendingInteraction {
}
