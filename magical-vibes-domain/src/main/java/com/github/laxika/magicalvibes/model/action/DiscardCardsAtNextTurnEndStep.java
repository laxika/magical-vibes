package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.List;
import java.util.UUID;

/** Delayed trigger that discards a remembered set of cards at the controller's next end step. */
public record DiscardCardsAtNextTurnEndStep(UUID playerId, List<UUID> cardIds, Card sourceCard,
                                             UUID controllerId, int registeredTurnNumber)
        implements DelayedAction {

    public DiscardCardsAtNextTurnEndStep {
        cardIds = cardIds == null ? List.of() : List.copyOf(cardIds);
    }
}
