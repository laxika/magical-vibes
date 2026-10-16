package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.EachPlayerMayPutCardFromHandToBattlefieldEffect;

import java.util.List;
import java.util.UUID;

/**
 * The rest of an "each player may put a card from their hand onto the battlefield" process that
 * is waiting for the prompt opened by the card just put (an as-enters choice or an Aura's host
 * choice) to be answered, so the next player's choice is not offered over it.
 *
 * @param remainingPlayerIds players still to be asked, in order
 * @param chosenCardIds      cards already chosen for the simultaneous mode
 * @param effect             the effect driving the process
 * @param cardName           name of the source card, for prompts and the log
 * @param cardPutThisRound   whether a card was put during the current round of the repeating mode
 * @param startingPlayerId   the controller-first starting player, or {@code null}
 */
public record PendingEachPlayerPutContinuation(List<UUID> remainingPlayerIds, List<UUID> chosenCardIds,
                                               EachPlayerMayPutCardFromHandToBattlefieldEffect effect,
                                               String cardName, boolean cardPutThisRound,
                                               UUID startingPlayerId) {

    public PendingEachPlayerPutContinuation {
        remainingPlayerIds = List.copyOf(remainingPlayerIds);
        chosenCardIds = List.copyOf(chosenCardIds);
    }
}
