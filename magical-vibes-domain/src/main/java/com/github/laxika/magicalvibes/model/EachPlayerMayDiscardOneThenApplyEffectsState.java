package com.github.laxika.magicalvibes.model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

/**
 * Progress state for an each-player may-discard-one effect with shared discard-type riders. The
 * picks themselves are held in {@link GameData#deferredDiscardSelections} until every player chose.
 */
public class EachPlayerMayDiscardOneThenApplyEffectsState {

    public boolean active;
    public UUID controllerId;
    public UUID currentPlayerId;
    public final Deque<UUID> remaining = new ArrayDeque<>();

    public void reset() {
        active = false;
        controllerId = null;
        currentPlayerId = null;
        remaining.clear();
    }
}
