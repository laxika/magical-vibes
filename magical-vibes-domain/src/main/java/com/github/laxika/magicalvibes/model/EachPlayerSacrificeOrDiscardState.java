package com.github.laxika.magicalvibes.model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

/** Progress state for a resolving each-player discard-or-sacrifice effect. */
public class EachPlayerSacrificeOrDiscardState {

    public boolean active;
    public final Deque<UUID> remaining = new ArrayDeque<>();
    public UUID currentPlayerId;
    public String chosenMode;

    public void reset() {
        active = false;
        remaining.clear();
        currentPlayerId = null;
        chosenMode = null;
    }
}
