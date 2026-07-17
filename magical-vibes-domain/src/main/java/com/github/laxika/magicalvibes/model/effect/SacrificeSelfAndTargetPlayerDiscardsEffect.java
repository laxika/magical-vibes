package com.github.laxika.magicalvibes.model.effect;

/**
 * "Sacrifice this creature. If you do, that player discards N cards."
 * Used inside a MayEffect wrapper for unblocked-attack triggers where "that player" is the defending player.
 * Context: StackEntry.targetId = defending player ID, StackEntry.sourcePermanentId = source creature ID.
 */
public record SacrificeSelfAndTargetPlayerDiscardsEffect(int amount) implements CardEffect {
}
