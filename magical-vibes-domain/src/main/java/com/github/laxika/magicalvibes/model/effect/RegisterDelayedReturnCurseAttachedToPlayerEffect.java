package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Registers the delayed return of a Curse to the battlefield attached to a player. */
public record RegisterDelayedReturnCurseAttachedToPlayerEffect(
        UUID cardId,
        UUID attachedPlayerId
) implements CardEffect {
}
