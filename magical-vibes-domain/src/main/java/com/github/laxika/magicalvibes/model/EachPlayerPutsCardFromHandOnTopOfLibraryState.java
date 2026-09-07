package com.github.laxika.magicalvibes.model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

/** Progress state for an APNAP each-player hand-card-to-library-top choice flow. */
public class EachPlayerPutsCardFromHandOnTopOfLibraryState {

    public boolean active;
    public final Deque<UUID> remaining = new ArrayDeque<>();

    public void reset() {
        active = false;
        remaining.clear();
    }
}
