package com.github.laxika.magicalvibes.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Progress state for an APNAP each-player graveyard-exile choice. */
public class EachPlayerMayExileGraveyardCardsState {

    public boolean active;
    public UUID currentPlayerId;
    public final Deque<UUID> remaining = new ArrayDeque<>();
    /** Cards each player chose to exile; they leave the graveyards together once every player has chosen (CR 101.4). */
    public final Map<UUID, List<UUID>> chosenCardIds = new LinkedHashMap<>();

    public void reset() {
        active = false;
        currentPlayerId = null;
        remaining.clear();
        chosenCardIds.clear();
    }

    public void recordChoice(UUID playerId, List<UUID> cardIds) {
        chosenCardIds.put(playerId, new ArrayList<>(cardIds));
    }
}
