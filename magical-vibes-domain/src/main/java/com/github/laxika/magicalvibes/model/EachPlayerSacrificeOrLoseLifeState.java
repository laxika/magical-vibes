package com.github.laxika.magicalvibes.model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

/** Progress state for an APNAP each-player sacrifice-or-life-loss effect. */
public class EachPlayerSacrificeOrLoseLifeState {

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
