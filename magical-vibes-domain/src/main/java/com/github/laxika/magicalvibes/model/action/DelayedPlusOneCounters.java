package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/**
 * Delayed trigger: {@code totalCounters} +1/+1 counters to put on {@code permanentId} at the beginning
 * of the next end step (Protean Hydra's regrowth), controlled by {@code controllerId} (the controller of
 * the regrowth ability when it resolved; {@code null} means the permanent's current controller).
 * Accumulator semantics (one entry per permanent and controller) are preserved by
 * {@code GameData.addDelayedPlusOneCounters}.
 */
public record DelayedPlusOneCounters(UUID permanentId, int totalCounters, UUID controllerId) implements DelayedAction {

    public DelayedPlusOneCounters(UUID permanentId, int totalCounters) {
        this(permanentId, totalCounters, null);
    }
}
