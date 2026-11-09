package com.github.laxika.magicalvibes.model.effect;

/**
 * "Sacrifice this creature. If you do, that player discards a card for each poison counter they have."
 * Used inside a MayEffect wrapper for combat damage triggers: "that player" is the player dealt damage, the stack
 * entry's triggering player ({@code StackEntry.getTriggeringPlayerId()}), and "it" the source permanent.
 */
public record SacrificeSelfAndTargetDiscardsPerPoisonCounterEffect() implements CardEffect {
}
