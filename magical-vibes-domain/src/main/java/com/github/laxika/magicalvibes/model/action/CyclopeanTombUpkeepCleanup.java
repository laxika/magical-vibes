package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.Set;
import java.util.UUID;

/** Persistent delayed upkeep trigger created when a Cyclopean Tomb leaves the battlefield. */
public record CyclopeanTombUpkeepCleanup(
        UUID actionId,
        Card sourceCard,
        UUID controllerId,
        Set<UUID> trackedLandIds,
        Set<UUID> removedLandIds,
        UUID sourcePermanentId
) implements DelayedAction {

    public CyclopeanTombUpkeepCleanup(UUID actionId, Card sourceCard, UUID controllerId,
                                    Set<UUID> trackedLandIds, Set<UUID> removedLandIds) {
        this(actionId, sourceCard, controllerId, trackedLandIds, removedLandIds, null);
    }

    public CyclopeanTombUpkeepCleanup withTrackedLandIds(Set<UUID> ids) {
        return new CyclopeanTombUpkeepCleanup(actionId, sourceCard, controllerId, ids, removedLandIds, sourcePermanentId);
    }

    public CyclopeanTombUpkeepCleanup {
        trackedLandIds = Set.copyOf(trackedLandIds);
        removedLandIds = Set.copyOf(removedLandIds);
    }

    public CyclopeanTombUpkeepCleanup withLandRemoved(UUID landId) {
        java.util.HashSet<UUID> removed = new java.util.HashSet<>(removedLandIds);
        removed.add(landId);
        return new CyclopeanTombUpkeepCleanup(actionId, sourceCard, controllerId, trackedLandIds, removed, sourcePermanentId);
    }
}
