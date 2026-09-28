package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/** Delayed end-step sacrifice that resolves only when the permanent's mana value is high enough. */
public record DelayedSacrificeTargetPermanentAtEndStepIfManaValueAtLeast(
        UUID permanentId,
        UUID controllerId,
        int minManaValue
) implements DelayedAction {
}
