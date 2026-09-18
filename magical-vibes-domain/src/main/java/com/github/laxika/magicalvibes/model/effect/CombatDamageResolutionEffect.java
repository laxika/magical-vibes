package com.github.laxika.magicalvibes.model.effect;

/**
 * Internal stack effect used to resolve combat damage that was assigned before it was put on the
 * stack by {@link CombatDamageUsesStackEffect}.
 */
public record CombatDamageResolutionEffect() implements CardEffect {
}
