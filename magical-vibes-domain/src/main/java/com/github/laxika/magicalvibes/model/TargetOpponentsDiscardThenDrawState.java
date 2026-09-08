package com.github.laxika.magicalvibes.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.UUID;

/** Progress state for an opponent discard sequence with a controller draw. */
public class TargetOpponentsDiscardThenDrawState {

    public boolean active;
    public boolean completed;
    public UUID controllerId;
    public final Deque<UUID> remainingTargetIds = new ArrayDeque<>();
    public final List<UUID> noDiscardPlayers = new ArrayList<>();
    public final List<SelectedDiscard> selectedDiscards = new ArrayList<>();

    public void reset() {
        active = false;
        completed = false;
        controllerId = null;
        remainingTargetIds.clear();
        noDiscardPlayers.clear();
        selectedDiscards.clear();
    }

    public record SelectedDiscard(UUID playerId, UUID cardId, int manaValue, boolean wasDiscarded) {
    }
}
