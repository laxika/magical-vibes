package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

/**
 * Matches a spell with a card type among {@code trackedTypes} that has no counter over it on the source's tracker list:
 * "a spell of a type showing on Bucket List". The tracker list belongs to the source permanent as it is now (a copy's
 * starts empty), so the predicate matches nothing when it is evaluated without a source.
 */
public record StackEntryHasTypeShowingOnSourceTrackerPredicate(Set<CardType> trackedTypes)
        implements StackEntryPredicate {

    public StackEntryHasTypeShowingOnSourceTrackerPredicate {
        trackedTypes = Set.copyOf(trackedTypes);
    }
}
