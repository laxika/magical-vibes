package com.github.laxika.magicalvibes.model.effect;

/**
 * One-shot effect: for the rest of the turn, if a creature would deal combat damage to a creature,
 * it deals double that damage to that creature instead. Resolving adds a floating effect on every
 * player that lasts until end of turn, read by
 * {@code GameQueryService.getCombatDamageToCreatureMultiplier} only when the damage recipient is a creature.
 * Multiple instances stack multiplicatively. Used by Blind Fury.
 */
public record DoubleCombatDamageToCreaturesThisTurnEffect() implements CardEffect, DoublingEffect {
}
