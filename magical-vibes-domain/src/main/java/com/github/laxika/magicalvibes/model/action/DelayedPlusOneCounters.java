package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/**
 * Delayed trigger: {@code totalCounters} +1/+1 counters to put on {@code permanentId} at the beginning
 * of the next end step (Protean Hydra's regrowth). Keyed-accumulator semantics (there is at most one
 * entry per permanent) are preserved by {@code GameData.addDelayedPlusOneCounters}.
 */
public record DelayedPlusOneCounters(UUID permanentId, int totalCounters) implements DelayedAction {
}
