package com.github.laxika.magicalvibes.model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

/**
 * Progress state for Winter's Chill's "for each targeted attacking creature, its controller may
 * pay {1} or {2}" flow. Driven one target at a time by {@code WintersChillEffectHandler}, which
 * re-runs after each payment choice via {@code rerunCurrentEffectAfterInteraction}.
 */
public class WintersChillState {

    public boolean active;
    /** Targeted creature ids still awaiting a payment choice. */
    public final Deque<UUID> remainingTargetIds = new ArrayDeque<>();
    /** Creature currently being decided on. */
    public UUID currentTargetId;
    /** Chosen payment option for the current target, consumed on the next re-entry. */
    public String chosenMode;

    public void reset() {
        active = false;
        remainingTargetIds.clear();
        currentTargetId = null;
        chosenMode = null;
    }
}
