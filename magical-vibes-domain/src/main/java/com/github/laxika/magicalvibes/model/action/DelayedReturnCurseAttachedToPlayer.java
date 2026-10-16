package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/**
 * Delayed return of a Curse to its owner's battlefield attached to a player.
 *
 * @param graveyardEntryVersion the Curse's graveyard entry identity when the return was scheduled; a card that
 *                              has since left and re-entered the graveyard is a new object and is not returned
 */
public record DelayedReturnCurseAttachedToPlayer(
        UUID cardId,
        UUID ownerId,
        UUID attachedPlayerId,
        long graveyardEntryVersion
) implements DelayedAction {
}
