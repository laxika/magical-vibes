package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.List;
import java.util.UUID;

/** Delayed trigger that remembers permanents to exile at the controller's next upkeep. */
public record ExilePermanentsAtNextUpkeep(UUID controllerId, List<UUID> permanentIds, Card sourceCard)
        implements DelayedAction {

    public ExilePermanentsAtNextUpkeep {
        permanentIds = List.copyOf(permanentIds);
    }
}
