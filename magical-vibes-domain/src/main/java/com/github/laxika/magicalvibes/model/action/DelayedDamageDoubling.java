package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/**
 * Recipient-scoped damage doubling that expires at the controller's next turn.
 */
public record DelayedDamageDoubling(UUID targetPlayerId, UUID controllerId) implements DelayedAction {
}
