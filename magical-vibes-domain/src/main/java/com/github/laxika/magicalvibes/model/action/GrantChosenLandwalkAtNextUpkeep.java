package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/**
 * Delayed trigger for a source creature's controller's next upkeep. The trigger is skipped if the
 * source permanent has left the battlefield before that upkeep.
 */
public record GrantChosenLandwalkAtNextUpkeep(UUID controllerId, UUID permanentId, Card sourceCard)
        implements DelayedAction {
}
