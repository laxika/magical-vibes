package com.github.laxika.magicalvibes.model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

/** Progress state for an APNAP each-player graveyard-exile choice. */
public class EachPlayerMayExileGraveyardCardsState {

    public boolean active;
    public UUID currentPlayerId;
    public final Deque<UUID> remaining = new ArrayDeque<>();

    public void reset() {
        active = false;
        currentPlayerId = null;
        remaining.clear();
    }
}
