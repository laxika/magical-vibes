package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/**
 * Delayed trigger for returning an Aura and choosing a legal creature to enchant.
 *
 * @param graveyardEntryVersion the Aura's graveyard stay when the return was set up; a card that left
 *                              the graveyard and came back is a new object the return can't find (CR 400.7)
 */
public record DelayedReturnSourceAuraToCreature(
        UUID auraCardId,
        UUID controllerId,
        long graveyardEntryVersion
) implements DelayedAction {
}
