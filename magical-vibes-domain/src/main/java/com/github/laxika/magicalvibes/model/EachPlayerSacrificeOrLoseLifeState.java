package com.github.laxika.magicalvibes.model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

/** Progress state for an APNAP each-player sacrifice-or-life-loss effect. */
public class EachPlayerSacrificeOrLoseLifeState {

    public boolean active;
    public final Deque<UUID> remaining = new ArrayDeque<>();
    public UUID currentPlayerId;
    public String chosenMode;
    public final List<UUID> sacrificeIds = new ArrayList<>();
    public final List<UUID> lifeLossPlayerIds = new ArrayList<>();

    public void reset() {
        active = false;
        remaining.clear();
        currentPlayerId = null;
        chosenMode = null;
        sacrificeIds.clear();
        lifeLossPlayerIds.clear();
    }
}
