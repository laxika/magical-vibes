package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.Set;
import java.util.UUID;

/** The recurring upkeep trigger created when a Cyclopean Tomb reaches its owner's graveyard. */
public record CyclopeanTombMireCleanup(UUID tombPermanentId, UUID controllerId, Card sourceCard,
                                       Set<UUID> landIds) implements DelayedAction {

    public CyclopeanTombMireCleanup {
        landIds = Set.copyOf(landIds);
    }
}
