package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: each player may cast at most one non-Phyrexian spell each turn. Phyrexian spells
 * are exempt. Enforced in {@code CastingPermissionService}.
 */
public record LimitNonPhyrexianSpellsPerTurnEffect() implements CardEffect {
}
