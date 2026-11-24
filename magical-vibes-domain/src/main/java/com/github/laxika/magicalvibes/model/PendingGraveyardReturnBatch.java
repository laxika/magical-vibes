package com.github.laxika.magicalvibes.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Cards selected by a controller before they enter the battlefield simultaneously.
 *
 * @param holdInGraveyard if {@code true}, chosen cards stay in their graveyards (keeping the graveyard
 *                        indices of later choices stable) until the last choice, then enter together with
 *                        Aura host choices made inside the batch (Gruesome Menagerie)
 */
public record PendingGraveyardReturnBatch(UUID controllerId, List<Card> cards,
                                          Map<UUID, UUID> graveyardOwnerByCardId,
                                          boolean underOwnersControl, boolean eachPlayerChooses,
                                          boolean enterTapped, CounterType enterWithCounter,
                                          CardColor grantColor, CardSubtype grantSubtype,
                                          Set<Keyword> grantKeywords, boolean holdInGraveyard) {

    public PendingGraveyardReturnBatch {
        cards = List.copyOf(cards);
        graveyardOwnerByCardId = Map.copyOf(graveyardOwnerByCardId);
        grantKeywords = grantKeywords == null ? Set.of() : Set.copyOf(grantKeywords);
    }

    public PendingGraveyardReturnBatch(UUID controllerId, List<Card> cards,
                                       Map<UUID, UUID> graveyardOwnerByCardId,
                                       boolean underOwnersControl, boolean eachPlayerChooses,
                                       boolean enterTapped, CounterType enterWithCounter,
                                       CardColor grantColor, CardSubtype grantSubtype,
                                       Set<Keyword> grantKeywords) {
        this(controllerId, cards, graveyardOwnerByCardId, underOwnersControl, eachPlayerChooses,
                enterTapped, enterWithCounter, grantColor, grantSubtype, grantKeywords, false);
    }

    /** A batch for the controller's own graveyard whose chosen cards stay there until all enter together. */
    public static PendingGraveyardReturnBatch heldInGraveyard(UUID controllerId) {
        return new PendingGraveyardReturnBatch(controllerId, List.of(), Map.of(), false, false, false, null,
                null, null, Set.of(), true);
    }

    public PendingGraveyardReturnBatch(UUID controllerId, List<Card> cards,
                                       Map<UUID, UUID> graveyardOwnerByCardId,
                                       boolean underOwnersControl, boolean eachPlayerChooses,
                                       boolean enterTapped, CounterType enterWithCounter) {
        this(controllerId, cards, graveyardOwnerByCardId, underOwnersControl, eachPlayerChooses,
                enterTapped, enterWithCounter, null, null, Set.of());
    }

    public PendingGraveyardReturnBatch(UUID controllerId, List<Card> cards,
                                       Map<UUID, UUID> graveyardOwnerByCardId, boolean underOwnersControl) {
        this(controllerId, cards, graveyardOwnerByCardId, underOwnersControl, false, false, null,
                null, null, Set.of());
    }

    public PendingGraveyardReturnBatch(UUID controllerId, List<Card> cards,
                                       Map<UUID, UUID> graveyardOwnerByCardId) {
        this(controllerId, cards, graveyardOwnerByCardId, false);
    }

    public PendingGraveyardReturnBatch add(Card card, UUID graveyardOwnerId) {
        List<Card> updatedCards = new ArrayList<>(cards);
        updatedCards.add(card);
        Map<UUID, UUID> updatedOwners = new java.util.HashMap<>(graveyardOwnerByCardId);
        updatedOwners.put(card.getId(), graveyardOwnerId);
        return new PendingGraveyardReturnBatch(controllerId, updatedCards, updatedOwners, underOwnersControl,
                eachPlayerChooses, enterTapped, enterWithCounter, grantColor, grantSubtype, grantKeywords,
                holdInGraveyard);
    }
}
