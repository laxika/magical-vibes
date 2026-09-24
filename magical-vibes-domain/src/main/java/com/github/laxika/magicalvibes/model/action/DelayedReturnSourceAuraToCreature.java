package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/** Delayed trigger for returning an Aura and choosing a legal creature to enchant. */
public record DelayedReturnSourceAuraToCreature(
        UUID auraCardId,
        UUID controllerId
) implements DelayedAction {
}
