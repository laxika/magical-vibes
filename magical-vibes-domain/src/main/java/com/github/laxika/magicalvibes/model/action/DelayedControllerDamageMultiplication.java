package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/** A controller-scoped damage multiplier that expires at turn cleanup. */
public record DelayedControllerDamageMultiplication(UUID controllerId, int multiplier)
        implements DelayedAction {
}
