package com.github.laxika.magicalvibes.model.effect;

/**
 * "Sacrifice this creature. If you do, that player discards a card for each poison counter they have."
 * Used inside a MayEffect wrapper for combat damage triggers where "that player" is the damaged player.
 * Context: StackEntry.targetPermanentId = damaged player ID, StackEntry.sourcePermanentId = source creature ID.
 */
public record SacrificeSelfAndTargetDiscardsPerPoisonCounterEffect() implements CardEffect {
}
