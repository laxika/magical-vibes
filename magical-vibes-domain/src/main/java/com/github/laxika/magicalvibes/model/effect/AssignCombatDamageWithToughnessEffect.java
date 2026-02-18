package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect for equipment/auras: the equipped/enchanted creature assigns combat damage
 * equal to its toughness rather than its power, but only when its toughness is greater than
 * its power.
 */
public record AssignCombatDamageWithToughnessEffect() implements CardEffect {
}
