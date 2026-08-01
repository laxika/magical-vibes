package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: players can cast spells and activate abilities only during their own turns
 * (mana abilities included; all zones). Used by City of Solitude (VIS).
 */
public record PlayersCanCastAndActivateOnlyDuringOwnTurnEffect() implements CardEffect {
}
