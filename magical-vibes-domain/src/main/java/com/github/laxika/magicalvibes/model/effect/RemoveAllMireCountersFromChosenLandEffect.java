package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSpecificPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Chooses one eligible land at resolution and removes all of its mire counters: Cyclopean Tomb's "remove all mire
 * counters from a land that a mire counter was put onto with this artifact but that a mire counter has not been
 * removed from with this artifact". The lands are the ones its Tomb put a mire counter onto and the ones it has taken
 * the counters from since, so the delayed ability that holds it is replaced as either changes.
 *
 * @param delayedActionId the identity of the Tomb's delayed ability, which the land choice finds it by
 * @param trackedLandIds  the lands a mire counter was put onto with the Tomb
 * @param removedLandIds  the lands a mire counter has been removed from with the Tomb
 */
public record RemoveAllMireCountersFromChosenLandEffect(
        UUID delayedActionId,
        Set<UUID> trackedLandIds,
        Set<UUID> removedLandIds
) implements CardEffect {

    public RemoveAllMireCountersFromChosenLandEffect {
        trackedLandIds = Set.copyOf(trackedLandIds);
        removedLandIds = Set.copyOf(removedLandIds);
    }

    /** The lands one may be chosen from: tracked and not yet cleaned. */
    public Set<UUID> remainingLandIds() {
        Set<UUID> remaining = new HashSet<>(trackedLandIds);
        remaining.removeAll(removedLandIds);
        return remaining;
    }

    /** The filter of the lands {@link #remainingLandIds()} names. */
    public PermanentPredicate permanentFilter() {
        return new PermanentAllOfPredicate(List.of(
                new PermanentIsLandPredicate(),
                new PermanentAnyOfPredicate(remainingLandIds().stream()
                        .<PermanentPredicate>map(PermanentIsSpecificPermanentPredicate::new)
                        .toList())));
    }

    /** This effect once a mire counter has been removed from {@code landId} with the Tomb. */
    public RemoveAllMireCountersFromChosenLandEffect withLandRemoved(UUID landId) {
        Set<UUID> removed = new HashSet<>(removedLandIds);
        removed.add(landId);
        return new RemoveAllMireCountersFromChosenLandEffect(delayedActionId, trackedLandIds, removed);
    }

    /** This effect once a mire counter has been put onto {@code landIds} with the Tomb. */
    public RemoveAllMireCountersFromChosenLandEffect withLandsTracked(Set<UUID> landIds) {
        Set<UUID> tracked = new HashSet<>(trackedLandIds);
        tracked.addAll(landIds);
        return new RemoveAllMireCountersFromChosenLandEffect(delayedActionId, tracked, removedLandIds);
    }

    /** "At the beginning of each of your upkeeps, remove all mire counters from a land ...". */
    public TriggeredAbility atEachOfYourUpkeeps() {
        return new TriggeredAbility(Triggers.upkeep(ActivePlayer.YOU), this);
    }
}
