package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/** Delayed return of a Curse to its owner's battlefield attached to a player. */
public record DelayedReturnCurseAttachedToPlayer(
        UUID cardId,
        UUID ownerId,
        UUID attachedPlayerId
) implements DelayedAction {
}
