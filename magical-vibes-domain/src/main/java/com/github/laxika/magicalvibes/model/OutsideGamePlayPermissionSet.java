package com.github.laxika.magicalvibes.model;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The cards a player may currently play from outside the game. Cards granted by one resolution of
 * "you may play a card you own from outside the game" form a single-play group: removing any one
 * of them (because it was played) also withdraws the rest of that group's cards, unless another
 * group still permits them. Cards added individually are permitted on their own.
 */
public final class OutsideGamePlayPermissionSet extends AbstractSet<UUID> {

    private final Set<UUID> permitted = ConcurrentHashMap.newKeySet();
    private final List<Set<UUID>> singlePlayGroups = new CopyOnWriteArrayList<>();

    /** Permits each card, but only one of them may be played for this grant. */
    public void addSinglePlayGroup(Collection<UUID> cardIds) {
        if (cardIds.isEmpty()) {
            return;
        }
        singlePlayGroups.add(Set.copyOf(cardIds));
        permitted.addAll(cardIds);
    }

    /** Replaces this set's contents with an independent copy of {@code other}'s. */
    public void copyFrom(OutsideGamePlayPermissionSet other) {
        permitted.clear();
        singlePlayGroups.clear();
        permitted.addAll(other.permitted);
        singlePlayGroups.addAll(other.singlePlayGroups);
    }

    @Override
    public boolean add(UUID cardId) {
        return permitted.add(cardId);
    }

    @Override
    public boolean contains(Object cardId) {
        return permitted.contains(cardId);
    }

    @Override
    public boolean remove(Object cardId) {
        boolean removed = permitted.remove(cardId);
        if (removed) {
            singlePlayGroups.stream()
                    .filter(group -> group.contains(cardId))
                    .findFirst()
                    .ifPresent(this::spend);
        }
        return removed;
    }

    private void spend(Set<UUID> group) {
        singlePlayGroups.remove(group);
        for (UUID other : group) {
            if (singlePlayGroups.stream().noneMatch(remaining -> remaining.contains(other))) {
                permitted.remove(other);
            }
        }
    }

    @Override
    public void clear() {
        permitted.clear();
        singlePlayGroups.clear();
    }

    @Override
    public int size() {
        return permitted.size();
    }

    @Override
    public Iterator<UUID> iterator() {
        Iterator<UUID> snapshot = new ArrayList<>(permitted).iterator();
        return new Iterator<>() {
            private UUID last;

            @Override
            public boolean hasNext() {
                return snapshot.hasNext();
            }

            @Override
            public UUID next() {
                last = snapshot.next();
                return last;
            }

            @Override
            public void remove() {
                OutsideGamePlayPermissionSet.this.remove(last);
            }
        };
    }
}
