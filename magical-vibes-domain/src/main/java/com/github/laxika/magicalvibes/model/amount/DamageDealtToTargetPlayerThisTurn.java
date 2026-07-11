package com.github.laxika.magicalvibes.model.amount;

/**
 * The total damage already dealt to the targeted player this turn (from any source — combat, spells,
 * abilities; includes damage dealt as poison), read from {@code GameData.damageDealtToPlayersThisTurn}.
 * The target player's id comes from the stack entry's target channel. Used by Final Punishment.
 */
public record DamageDealtToTargetPlayerThisTurn() implements DynamicAmount {
}
