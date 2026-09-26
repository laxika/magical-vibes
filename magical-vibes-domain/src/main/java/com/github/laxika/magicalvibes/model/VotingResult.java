package com.github.laxika.magicalvibes.model;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Immutable snapshot of each player's choices in one completed voting event. */
public record VotingResult(Map<UUID, Set<String>> choicesByPlayer) {

    public VotingResult {
        Map<UUID, Set<String>> copy = new LinkedHashMap<>();
        choicesByPlayer.forEach((playerId, choices) ->
                copy.put(playerId, Set.copyOf(new LinkedHashSet<>(choices))));
        choicesByPlayer = Map.copyOf(copy);
    }
}
