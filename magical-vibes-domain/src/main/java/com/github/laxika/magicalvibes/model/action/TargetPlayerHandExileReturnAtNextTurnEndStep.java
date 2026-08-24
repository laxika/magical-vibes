package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.List;
import java.util.UUID;

/** Delayed trigger that returns a target player's remembered exiled hand cards at that player's next turn's end step. */
public record TargetPlayerHandExileReturnAtNextTurnEndStep(
        UUID playerId,
        List<UUID> cardIds,
        Card sourceCard,
        UUID controllerId,
        int registeredTurnNumber) implements DelayedAction {

    public TargetPlayerHandExileReturnAtNextTurnEndStep {
        cardIds = cardIds == null ? List.of() : List.copyOf(cardIds);
    }
}
