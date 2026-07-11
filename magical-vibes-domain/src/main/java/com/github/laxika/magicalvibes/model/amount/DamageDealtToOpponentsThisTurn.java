package com.github.laxika.magicalvibes.model.amount;

/**
 * The total damage dealt to the controller's opponents this turn (from any source — combat, spells,
 * abilities; includes damage dealt as poison), summed over every opponent, read from
 * {@code GameData.damageDealtToPlayersThisTurn}. Used by Notorious Throng ("X is the damage dealt to
 * your opponents this turn").
 */
public record DamageDealtToOpponentsThisTurn() implements DynamicAmount {
}
