package com.github.laxika.magicalvibes.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.UUID;

/** Progress state for Disorienting Choice's per-target optional exile decisions. */
public class DisorientingChoiceState {

    public boolean active;
    public final Deque<UUID> remainingTargetIds = new ArrayDeque<>();
    public final List<UUID> selectedTargetIds = new ArrayList<>();
    public UUID currentTargetId;
    public String chosenMode;

    public void reset() {
        active = false;
        remainingTargetIds.clear();
        selectedTargetIds.clear();
        currentTargetId = null;
        chosenMode = null;
    }
}
