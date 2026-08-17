package com.github.laxika.magicalvibes.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.UUID;

/** Progress state for Plaguecrafter's simultaneous sacrifice-then-discard resolution. */
public class PlaguecrafterState {

    public boolean active;
    public boolean sacrificeChoicesInProgress;
    public boolean completed;
    public UUID sourceControllerId;
    public final List<UUID> playersWhoCannotSacrifice = new ArrayList<>();
    public final Deque<UUID> remainingDiscardPlayers = new ArrayDeque<>();
    public final List<SelectedDiscard> selectedDiscards = new ArrayList<>();

    public void reset() {
        active = false;
        sacrificeChoicesInProgress = false;
        completed = false;
        sourceControllerId = null;
        playersWhoCannotSacrifice.clear();
        remainingDiscardPlayers.clear();
        selectedDiscards.clear();
    }

    public record SelectedDiscard(UUID playerId, UUID cardId) {
    }
}
