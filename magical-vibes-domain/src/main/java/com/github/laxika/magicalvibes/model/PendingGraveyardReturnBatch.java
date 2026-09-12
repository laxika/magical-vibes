package com.github.laxika.magicalvibes.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Cards selected by a controller before they enter the battlefield simultaneously. */
public record PendingGraveyardReturnBatch(UUID controllerId, List<Card> cards,
                                          Map<UUID, UUID> graveyardOwnerByCardId,
                                          boolean underOwnersControl, boolean eachPlayerChooses,
                                          boolean enterTapped, CounterType enterWithCounter) {

    public PendingGraveyardReturnBatch(UUID controllerId, List<Card> cards,
                                       Map<UUID, UUID> graveyardOwnerByCardId, boolean underOwnersControl) {
        this(controllerId, cards, graveyardOwnerByCardId, underOwnersControl, false, false, null);
    }

    public PendingGraveyardReturnBatch(UUID controllerId, List<Card> cards,
                                       Map<UUID, UUID> graveyardOwnerByCardId) {
        this(controllerId, cards, graveyardOwnerByCardId, false);
    }

    public PendingGraveyardReturnBatch {
        cards = List.copyOf(cards);
        graveyardOwnerByCardId = Map.copyOf(graveyardOwnerByCardId);
    }

    public PendingGraveyardReturnBatch add(Card card, UUID graveyardOwnerId) {
        List<Card> updatedCards = new ArrayList<>(cards);
        updatedCards.add(card);
        Map<UUID, UUID> updatedOwners = new java.util.HashMap<>(graveyardOwnerByCardId);
        updatedOwners.put(card.getId(), graveyardOwnerId);
        return new PendingGraveyardReturnBatch(controllerId, updatedCards, updatedOwners, underOwnersControl,
                eachPlayerChooses, enterTapped, enterWithCounter);
    }
}
