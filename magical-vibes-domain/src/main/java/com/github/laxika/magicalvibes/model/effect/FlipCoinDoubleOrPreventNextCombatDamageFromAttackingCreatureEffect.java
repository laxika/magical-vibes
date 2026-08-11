package com.github.laxika.magicalvibes.model.effect;

/**
 * Attack trigger: flip a coin. If the flip is won, the next time the triggering creature would deal
 * combat damage this turn, it deals double that damage instead. If the flip is lost, that damage is
 * prevented (Impulsive Maneuvers). The trigger's non-targeting {@code targetId} carries the attacking
 * creature.
 */
public record FlipCoinDoubleOrPreventNextCombatDamageFromAttackingCreatureEffect() implements CardEffect {
}
