package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/** A source-specific damage multiplier that expires at its controller's next turn. */
public record DelayedSourceDamageMultiplication(UUID sourcePermanentId, UUID controllerId,
                                                int multiplier) implements DelayedAction {
}
