package com.github.laxika.magicalvibes.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class RetetherOperationState {

    public final Deque<RetetherAuraChoiceRequest> pendingAuraChoices = new ArrayDeque<>();
    public final List<RetetherAuraPlacement> pendingPlacements = new ArrayList<>();
    public RetetherAuraChoiceRequest activeChoice;

    public boolean isActive() {
        return activeChoice != null || !pendingAuraChoices.isEmpty() || !pendingPlacements.isEmpty();
    }
}
