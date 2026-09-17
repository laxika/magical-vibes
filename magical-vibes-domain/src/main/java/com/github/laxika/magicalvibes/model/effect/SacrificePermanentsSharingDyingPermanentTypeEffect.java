package com.github.laxika.magicalvibes.model.effect;

/**
 * Trigger-only marker for Martyr's Bond. The trigger collector snapshots the dying permanent's
 * effective permanent card types and turns them into an each-opponent sacrifice effect.
 */
public record SacrificePermanentsSharingDyingPermanentTypeEffect() implements CardEffect {
}
