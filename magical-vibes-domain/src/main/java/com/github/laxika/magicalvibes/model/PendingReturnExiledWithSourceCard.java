package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * Marks that the active {@code LIBRARY_REVEAL_CHOICE} is the controller choosing one card exiled
 * "with" a source permanent to return from exile (Endless Horizons upkeep, Purgatory upkeep). Only
 * the selected card leaves exile; the rest stay exiled.
 *
 * <p>{@code toBattlefield} sends the chosen card to the battlefield instead of the hand; it enters
 * under {@code controllerId}'s control per CR 110.2a, which may differ from its owner.
 */
public record PendingReturnExiledWithSourceCard(boolean toBattlefield, UUID controllerId)
        implements PendingInteraction {

    public PendingReturnExiledWithSourceCard() {
        this(false, null);
    }
}
