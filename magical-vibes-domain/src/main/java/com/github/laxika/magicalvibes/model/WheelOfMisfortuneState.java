package com.github.laxika.magicalvibes.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Progress state for Wheel of Misfortune's hidden number choices. */
public class WheelOfMisfortuneState {

    public boolean active;
    public final List<UUID> order = new ArrayList<>();
    public int index;
    public final Map<UUID, Integer> chosenNumbers = new LinkedHashMap<>();
    public UUID currentPlayerId;

    public void reset() {
        active = false;
        order.clear();
        index = 0;
        chosenNumbers.clear();
        currentPlayerId = null;
    }
}
