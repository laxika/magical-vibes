package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: during combat, no player can cast spells. Symmetric and global. Used by
 * Basandra, Battle Seraph (VMA).
 */
public record PlayersCantCastSpellsDuringCombatEffect() implements CardEffect {
}
