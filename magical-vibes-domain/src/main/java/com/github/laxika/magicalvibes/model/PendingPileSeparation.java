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
 * puts the chosen pile into the controller's hand and the other into their graveyard;
 * {@code HAND_WITH_FACE_DOWN_PILE} puts the chosen pile into the controller's hand and the other
 * into their graveyard. Curator of Destinies uses a face-up Pile 1 and a face-down Pile 2;
 * Fortune's Favor reverses those pile identities. The other dispositions are ignored for
 * permanent-pile mode.
 *
 * <p>{@code controllerChoosesPile} is true for the usual Fact-or-Fiction-style flow, where the
 * opponent separates and the controller chooses. It is false for Steam Augury, where the
 * controller separates and the opponent chooses.
 * puts the chosen pile into the controller's hand and the other into their graveyard;
 * {@code OPPONENT_CHOOSES_EXILE} (Death or Glory) lets the opponent choose the pile to exile and
 * returns the other pile to the battlefield; {@code ATTACKERS} (Fight or Flight) makes the chosen
 * pile the only creatures that can attack this turn; {@code BLOCKERS} (Stand or Fall) makes the
 * chosen pile the only creatures that can block this turn; {@code DESTROY} (Do or Die) destroys
 * the creatures in the chosen pile without allowing regeneration. The other dispositions are
 * ignored for permanent-pile mode.
 */
public record PendingPileSeparation(UUID controllerId, UUID targetPlayerId,
                                    List<UUID> allPermanentIds,
                                    List<Card> cards, Map<UUID, UUID> cardOwners,
                                    List<UUID> pile1Ids, List<UUID> pile2Ids,
                                    CardPileDisposition disposition,
                                    boolean controllerChoosesPile)
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
                CardPileDisposition.BATTLEFIELD, true);
    }

    /** Card-pile variant with the usual opponent-separates/controller-chooses roles. */
    public PendingPileSeparation(UUID controllerId, UUID targetPlayerId,
                                 List<UUID> allPermanentIds,
                                 List<Card> cards, Map<UUID, UUID> cardOwners,
                                 List<UUID> pile1Ids, List<UUID> pile2Ids,
                                 CardPileDisposition disposition) {
        this(controllerId, targetPlayerId, allPermanentIds, cards, cardOwners, pile1Ids, pile2Ids,
                disposition, true);
    }

    /** Card-pile mode (Boneyard Parley, Brilliant Ultimatum, Unesh) when the held-out card list is non-empty. */
    public boolean cardPileMode() {
        return !cards.isEmpty();
    }
}
