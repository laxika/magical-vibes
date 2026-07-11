package com.github.laxika.magicalvibes.model.amount;

/**
 * The number of creatures put into a graveyard from the battlefield this turn, summed over
 * the players in scope (tracked per controller in {@code GameData.creatureDeathCountThisTurn}).
 */
public record CreatureDeathsThisTurn(CountScope scope) implements DynamicAmount {
}
