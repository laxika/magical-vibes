package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/**
 * Delayed offer to cast a spell exiled by rebound at the beginning of its controller's next upkeep.
 *
 * @param exileInsteadOfGraveyard whether the spell cast this way is exiled instead of being put into its owner's
 *                                graveyard (Power Pack); the rider is applied when the upkeep trigger fires, so no
 *                                cleanup step in between ends it
 */
public record ReboundAtNextUpkeep(UUID controllerId, UUID ownerId, Card card, boolean exileInsteadOfGraveyard)
        implements DelayedAction {

    public ReboundAtNextUpkeep(UUID controllerId, UUID ownerId, Card card) {
        this(controllerId, ownerId, card, false);
    }
}
