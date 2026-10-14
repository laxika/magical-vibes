package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/**
 * A controller-scoped damage multiplier that expires at turn cleanup. When {@code opponentPermanents}
 * is false, only damage dealt to opponents themselves is multiplied.
 */
public record DelayedControllerDamageMultiplication(UUID controllerId, int multiplier, boolean opponentPermanents)
        implements DelayedAction {
}
