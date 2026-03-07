package com.github.laxika.magicalvibes.model.effect;

/**
 * "Sacrifice this creature. If you do, draw N cards."
 * Used inside a MayEffect wrapper for combat damage triggers.
 * Context: StackEntry.sourcePermanentId = source creature ID.
 */
public record SacrificeSelfAndDrawCardsEffect(int amount) implements CardEffect {
}
