package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/**
 * Delayed trigger that returns an Aura card from its owner's graveyard to the battlefield attached
 * to a particular permanent under its owner's control.
 */
public record DelayedReturnAuraAttachedToPermanent(
        UUID auraCardId,
        UUID auraOwnerId,
        UUID enchantedPermanentId
) implements DelayedAction {
}
