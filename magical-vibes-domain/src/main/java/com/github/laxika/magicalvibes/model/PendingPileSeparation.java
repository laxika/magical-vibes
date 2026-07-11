package com.github.laxika.magicalvibes.model;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Pile-separation flow state, shared by permanent-pile effects (Liliana of the Veil) and
 * card-pile effects (Boneyard Parley). The flow spans two interaction windows — the pile-1
 * selection (a multi-permanent / multi-graveyard choice) and the pile-choice may prompt — so
 * it waits on the unified queue rather than riding a single interaction record; step 1 polls
 * it and re-queues it with the piles filled, step 2 polls it to completion.
 *
 * <p>Card-pile mode when {@code cards} is non-empty: the pile IDs then refer to card UUIDs and
 * {@code cardOwners} maps card UUID → original owner UUID (for returning the unchosen pile to
 * owners' graveyards). Otherwise the pile IDs refer to permanent UUIDs drawn from
 * {@code allPermanentIds}.
 */
public record PendingPileSeparation(UUID controllerId, UUID targetPlayerId,
                                    List<UUID> allPermanentIds,
                                    List<Card> cards, Map<UUID, UUID> cardOwners,
                                    List<UUID> pile1Ids, List<UUID> pile2Ids)
        implements PendingInteraction {

    public PendingPileSeparation {
        allPermanentIds = List.copyOf(allPermanentIds);
        cards = List.copyOf(cards);
        cardOwners = Map.copyOf(cardOwners);
        pile1Ids = List.copyOf(pile1Ids);
        pile2Ids = List.copyOf(pile2Ids);
    }

    /** Card-pile mode (Boneyard Parley) when the held-out card list is non-empty. */
    public boolean cardPileMode() {
        return !cards.isEmpty();
    }
}
