package com.github.laxika.magicalvibes.model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Progress state for Eumidian Wastewaker's attack trigger. */
public class EumidianWastewakerState {

    public boolean active;
    public UUID currentPlayerId;
    public final Deque<UUID> remaining = new ArrayDeque<>();
    public String chosenMode;
    public boolean pendingDiscard;
    public boolean pendingSacrificeChoice;
    public final Set<UUID> pendingSacrificeLandIds = new HashSet<>();
    public int landCardsPutIntoGraveyard;

    public void reset() {
        active = false;
        currentPlayerId = null;
        remaining.clear();
        chosenMode = null;
        pendingDiscard = false;
        pendingSacrificeChoice = false;
        pendingSacrificeLandIds.clear();
        landCardsPutIntoGraveyard = 0;
    }
}
