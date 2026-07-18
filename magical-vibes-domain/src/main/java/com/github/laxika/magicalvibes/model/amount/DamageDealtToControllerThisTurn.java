package com.github.laxika.magicalvibes.model.amount;

/**
 * The total damage dealt to the effect's controller this turn (from any source — combat, spells,
 * abilities; includes damage dealt as poison), read from {@code GameData.damageDealtToPlayersThisTurn}.
 * The controller-side counterpart of {@link DamageDealtToTargetPlayerThisTurn}. Used by Simulacrum
 * ("equal to the damage dealt to you this turn").
 */
public record DamageDealtToControllerThisTurn() implements DynamicAmount {
}
