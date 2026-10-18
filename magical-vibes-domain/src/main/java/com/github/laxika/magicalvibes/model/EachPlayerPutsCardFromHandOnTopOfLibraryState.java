package com.github.laxika.magicalvibes.model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Progress state for an APNAP each-player hand-card-to-library-top choice flow. */
public class EachPlayerPutsCardFromHandOnTopOfLibraryState {

    public boolean active;
    public final Deque<UUID> remaining = new ArrayDeque<>();

    /** Chosen hand card id per player, held back so every card moves simultaneously once all players chose. */
    public final Map<UUID, UUID> chosen = new LinkedHashMap<>();

    public void reset() {
        active = false;
        remaining.clear();
        chosen.clear();
    }
}
