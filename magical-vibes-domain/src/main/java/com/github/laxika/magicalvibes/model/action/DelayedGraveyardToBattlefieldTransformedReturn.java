package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/** Delayed trigger: card UUID to owner/controller UUIDs, return from graveyard transformed at the beginning
 *  of the next end step. Used by Loyal Cathar. {@code graveyardEntryVersion} identifies the card's graveyard stay
 *  when the trigger was registered, so a card that left and re-entered a graveyard (a new object, CR 400.7) is
 *  not returned. */
public record DelayedGraveyardToBattlefieldTransformedReturn(UUID cardId, UUID ownerId, UUID controllerId,
                                                              long graveyardEntryVersion) implements DelayedAction {
}
