package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/** Delayed trigger that exiles a returned permanent at the beginning of its controller's next upkeep. */
public record ExilePermanentAtNextUpkeep(UUID controllerId, UUID permanentId, Card sourceCard)
        implements DelayedAction {
}
