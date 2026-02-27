package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker interface for effects that represent additional costs of an activated ability
 * (sacrifice, discard, exile, counter removal, etc.). Cost effects are filtered out
 * during effect snapshotting and excluded from mana ability detection.
 */
public interface CostEffect extends CardEffect {
}
