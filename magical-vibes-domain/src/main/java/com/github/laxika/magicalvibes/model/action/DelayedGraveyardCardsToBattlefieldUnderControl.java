package com.github.laxika.magicalvibes.model.action;

import java.util.List;
import java.util.UUID;

/**
 * Delayed return of multiple cards from graveyards to the battlefield under one player's control.
 * The cards enter simultaneously when the action is processed.
 */
public record DelayedGraveyardCardsToBattlefieldUnderControl(
        List<UUID> cardIds,
        UUID controllerId
) implements DelayedAction {

    public DelayedGraveyardCardsToBattlefieldUnderControl {
        cardIds = List.copyOf(cardIds);
    }
}
