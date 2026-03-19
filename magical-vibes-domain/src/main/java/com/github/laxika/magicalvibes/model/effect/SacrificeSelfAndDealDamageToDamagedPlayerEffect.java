package com.github.laxika.magicalvibes.model.effect;

/**
 * "Sacrifice this creature. If you do, it deals N damage to that player."
 * Used inside a MayEffect wrapper for combat damage triggers where "that player" is the damaged player.
 * Context: StackEntry.targetId = damaged player ID, StackEntry.sourcePermanentId = source creature ID.
 */
public record SacrificeSelfAndDealDamageToDamagedPlayerEffect(int damage) implements CardEffect {
}
