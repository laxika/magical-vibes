package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: the players selected by {@code scope} can't cast more than {@code maxSpells}
 * spells each turn. Enforced in {@code CastingPermissionService.getMaxSpellsPerTurn}; when several
 * limits apply to the same player the most restrictive (lowest) value wins.
 */
public record LimitSpellsPerTurnEffect(int maxSpells, SpellLimitScope scope) implements CardEffect {
}
