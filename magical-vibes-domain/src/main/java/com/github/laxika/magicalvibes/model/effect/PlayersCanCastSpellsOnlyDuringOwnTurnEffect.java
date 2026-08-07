package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: players can cast spells only during their own turns. Unlike
 * {@link PlayersCanCastAndActivateOnlyDuringOwnTurnEffect} this leaves activated abilities
 * untouched. Used by Dosan the Falling Leaf (CHK).
 */
public record PlayersCanCastSpellsOnlyDuringOwnTurnEffect() implements CardEffect {
}
