package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/** Delayed trigger: card UUID to owner/controller UUIDs, return from graveyard transformed at the beginning
 *  of the next end step. Used by Loyal Cathar. */
public record DelayedGraveyardToBattlefieldTransformedReturn(UUID cardId, UUID ownerId, UUID controllerId) implements DelayedAction {
}
