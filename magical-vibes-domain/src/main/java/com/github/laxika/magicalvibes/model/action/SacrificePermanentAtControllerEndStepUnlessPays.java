package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/** Delayed end-step action that asks a permanent's controller to pay before sacrificing it. */
public record SacrificePermanentAtControllerEndStepUnlessPays(
        UUID permanentId,
        UUID controllerId,
        Card sourceCard,
        String manaCost
) implements DelayedAction {
}
