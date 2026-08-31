package com.github.laxika.magicalvibes.model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

/** Progress state for an effect that collects one discarded card from every player. */
public class EachPlayerDiscardsOneThenDrawsForEachCardTypeState {

    public boolean active;
    public UUID controllerId;
    public UUID currentPlayerId;
    public final Deque<UUID> remaining = new ArrayDeque<>();
    public final Set<CardType> discardedCardTypes = EnumSet.noneOf(CardType.class);

    public void reset() {
        active = false;
        controllerId = null;
        currentPlayerId = null;
        remaining.clear();
        discardedCardTypes.clear();
    }
}
