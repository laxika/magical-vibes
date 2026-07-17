package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/** Delayed trigger: exiled card UUID -> owner UUID, return from exile to the owner's hand at the
 *  beginning of the owner's next end step. Used by Necropotence's "Pay 1 life" ability. */
public record ReturnExiledCardToHandAtEndStep(UUID cardId, UUID ownerId) implements DelayedAction {
}
