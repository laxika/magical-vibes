package com.github.laxika.magicalvibes.model.effect;

/**
 * One-shot effect: for the rest of the turn, if a creature would deal combat damage to a creature,
 * it deals double that damage to that creature instead. Increments
 * {@code GameData.combatDamageToCreaturesDoublingsThisTurn} (cleared at turn cleanup), applied in
 * {@code GameQueryService.applyCombatDamageMultiplier} only when the damage recipient is a creature.
 * Multiple instances stack multiplicatively. Used by Blind Fury.
 */
public record DoubleCombatDamageToCreaturesThisTurnEffect() implements CardEffect {
}
