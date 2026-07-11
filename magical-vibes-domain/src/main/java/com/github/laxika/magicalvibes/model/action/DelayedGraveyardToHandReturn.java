package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/** Delayed trigger: card UUID -> owner UUID, return from graveyard to owner's hand at the beginning
 *  of the next end step. Used by Tiana, Ship's Caretaker. */
public record DelayedGraveyardToHandReturn(UUID cardId, UUID ownerId) implements DelayedAction {
}
