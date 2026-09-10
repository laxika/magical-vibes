package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/** Schedules a temporary control effect to expire during the controller's next cleanup step. */
public record ExpireControlAtEndOfNextTurn(UUID controlEffectId, UUID controllerId,
                                           int registeredTurnNumber) implements DelayedAction {
}
