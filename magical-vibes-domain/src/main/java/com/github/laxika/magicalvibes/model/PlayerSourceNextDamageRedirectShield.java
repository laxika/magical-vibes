package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import java.util.UUID;

/**
 * One-shot redirection shield for the next damage event from a chosen source to a player.
 */
public record PlayerSourceNextDamageRedirectShield(
        UUID protectedPlayerId,
        UUID sourcePermanentId,
        UUID redirectTargetPermanentId
) implements CardEffect {
}
