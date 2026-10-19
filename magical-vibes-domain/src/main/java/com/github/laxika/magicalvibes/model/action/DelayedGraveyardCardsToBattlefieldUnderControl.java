package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Delayed return of multiple remembered cards to the battlefield under one player's control, put on
 * the stack as a delayed triggered ability at the player's next upkeep. {@code graveyardEntryVersions}
 * holds the graveyard stay each card was remembered in; a card without a version was remembered in exile.
 */
public record DelayedGraveyardCardsToBattlefieldUnderControl(
        List<UUID> cardIds,
        UUID controllerId,
        Card sourceCard,
        Map<UUID, Long> graveyardEntryVersions
) implements DelayedAction {

    public DelayedGraveyardCardsToBattlefieldUnderControl {
        cardIds = List.copyOf(cardIds);
        graveyardEntryVersions = Map.copyOf(graveyardEntryVersions);
    }
}
