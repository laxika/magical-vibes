package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Trigger effect whose resolution needs the identity of the permanent that left the battlefield.
 */
public interface LeavingPermanentIdAwareEffect {

    CardEffect boundToLeavingPermanentId(UUID permanentId);
}
