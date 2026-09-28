package com.github.laxika.magicalvibes.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Progress state for Truth or Consequences' hidden votes. */
public class TruthOrConsequencesState {

    public boolean active;
    public final List<UUID> order = new ArrayList<>();
    public int index;
    public final Map<UUID, Integer> choices = new LinkedHashMap<>();
    public final List<Integer> voteChoices = new ArrayList<>();
    public UUID currentPlayerId;

    public void reset() {
        active = false;
        order.clear();
        index = 0;
        choices.clear();
        voteChoices.clear();
        currentPlayerId = null;
    }
}
