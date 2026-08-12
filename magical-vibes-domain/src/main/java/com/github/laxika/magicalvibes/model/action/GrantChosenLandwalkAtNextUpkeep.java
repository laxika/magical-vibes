package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/**
 * Delayed trigger for a source permanent's controller's next upkeep. The source permanent is
 * remembered by id, while the controller id remains the player who activated the ability.
 */
public record GrantChosenLandwalkAtNextUpkeep(UUID permanentId, UUID controllerId, Card sourceCard)
        implements DelayedAction {
}
