package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/**
 * Delayed trigger that exiles a returned permanent at the beginning of its controller's next upkeep. The permanents
 * one resolution of {@code sourceCard} schedules ({@code schedulingResolution}) are exiled by one delayed triggered
 * ability; another resolution of the same source created another (CR 603.7a).
 */
public record ExilePermanentAtNextUpkeep(UUID controllerId, UUID permanentId, Card sourceCard,
                                         long schedulingResolution)
        implements DelayedAction {

    public ExilePermanentAtNextUpkeep(UUID controllerId, UUID permanentId, Card sourceCard) {
        this(controllerId, permanentId, sourceCard, 0);
    }

    @Override
    public ExilePermanentAtNextUpkeep scheduledBy(long schedulingResolution) {
        return new ExilePermanentAtNextUpkeep(controllerId, permanentId, sourceCard, schedulingResolution);
    }
}
