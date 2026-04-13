package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * Composite key for tracking individual state-triggered abilities on the stack (CR 603.8).
 *
 * <p>A single permanent may have multiple state-triggered abilities. Tracking by
 * permanent ID alone would block all but the first from firing. This key combines
 * the permanent ID with the effect's index in the {@link EffectSlot#STATE_TRIGGERED}
 * list so each ability is tracked independently.</p>
 */
public record StateTriggerKey(UUID permanentId, int effectIndex) {
}
