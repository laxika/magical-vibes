package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/**
 * Delayed action that exiles a permanent at the beginning of its recorded controller's next end
 * step, skipping intervening end steps belonging to other players.
 */
public record ExilePermanentAtControllerEndStep(UUID permanentId, UUID controllerId)
        implements DelayedAction {
}
