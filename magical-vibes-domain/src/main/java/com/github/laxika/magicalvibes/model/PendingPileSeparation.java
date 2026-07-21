package com.github.laxika.magicalvibes.model;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Pile-separation flow state, shared by permanent-pile effects (Liliana of the Veil) and
 * card-pile effects (Boneyard Parley, Brilliant Ultimatum, Unesh's reveal-and-separate). The flow
 * spans two interaction windows — the pile-1 selection (a multi-permanent / multi-graveyard choice)
 * and the pile-choice may prompt — so it waits on the unified queue rather than riding a single
 * interaction record; step 1 polls it and re-queues it with the piles filled, step 2 polls it to
 * completion.
 *
 * <p>Card-pile mode when {@code cards} is non-empty: the pile IDs then refer to card UUIDs and
 * {@code cardOwners} maps card UUID → original owner UUID (for returning the unchosen pile to
 * owners' graveyards). Otherwise the pile IDs refer to permanent UUIDs drawn from
 * {@code allPermanentIds}.
 *
 * <p>{@code disposition} distinguishes the card-pile dispositions (see {@link CardPileDisposition}):
 * {@code BATTLEFIELD} (Boneyard Parley) puts the chosen pile onto the battlefield and returns the
 * rest to owners' graveyards; {@code PLAY_FROM_EXILE} (Brilliant Ultimatum) offers the chosen pile
 * to be played/cast for free from exile with everything else staying exiled; {@code HAND} (Unesh)
 * puts the chosen pile into the controller's hand and the other into their graveyard. It is ignored
 * for permanent-pile mode.
 */
public record PendingPileSeparation(UUID controllerId, UUID targetPlayerId,
                                    List<UUID> allPermanentIds,
                                    List<Card> cards, Map<UUID, UUID> cardOwners,
                                    List<UUID> pile1Ids, List<UUID> pile2Ids,
                                    CardPileDisposition disposition)
        implements PendingInteraction {

    public PendingPileSeparation {
        allPermanentIds = List.copyOf(allPermanentIds);
        cards = List.copyOf(cards);
        cardOwners = Map.copyOf(cardOwners);
        pile1Ids = List.copyOf(pile1Ids);
        pile2Ids = List.copyOf(pile2Ids);
    }

    /** Battlefield-disposition card-pile / permanent-pile variant (the pre-existing call sites). */
    public PendingPileSeparation(UUID controllerId, UUID targetPlayerId,
                                 List<UUID> allPermanentIds,
                                 List<Card> cards, Map<UUID, UUID> cardOwners,
                                 List<UUID> pile1Ids, List<UUID> pile2Ids) {
        this(controllerId, targetPlayerId, allPermanentIds, cards, cardOwners, pile1Ids, pile2Ids,
                CardPileDisposition.BATTLEFIELD);
    }

    /** Card-pile mode (Boneyard Parley, Brilliant Ultimatum, Unesh) when the held-out card list is non-empty. */
    public boolean cardPileMode() {
        return !cards.isEmpty();
    }
}
