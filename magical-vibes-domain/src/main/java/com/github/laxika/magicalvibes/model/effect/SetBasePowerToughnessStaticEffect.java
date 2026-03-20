package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect that sets the base power and toughness of permanents matching the given scope.
 * Used by auras like Deep Freeze ("enchanted creature has base power and toughness 0/4")
 * and similar effects (Darksteel Mutation, Frogify, Humility, etc.).
 * Modifiers (counters, static boosts) still apply on top of the new base values.
 *
 * @param power     the base power to set
 * @param toughness the base toughness to set
 * @param scope     which permanents are affected (ENCHANTED_CREATURE, EQUIPPED_CREATURE, etc.)
 */
public record SetBasePowerToughnessStaticEffect(int power, int toughness, GrantScope scope) implements CardEffect {
}
