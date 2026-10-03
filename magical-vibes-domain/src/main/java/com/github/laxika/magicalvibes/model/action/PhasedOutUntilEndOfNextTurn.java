package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/** Holds a directly phased-out permanent until the end of the activating player's next turn. */
public record PhasedOutUntilEndOfNextTurn(UUID permanentId, UUID controllerId,
                                          int registeredTurnNumber) implements DelayedAction {
}
