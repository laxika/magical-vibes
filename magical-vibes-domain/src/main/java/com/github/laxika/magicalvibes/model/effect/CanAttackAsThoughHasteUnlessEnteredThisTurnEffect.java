package com.github.laxika.magicalvibes.model.effect;

/**
 * Static self-scoped marker: this permanent can attack as though it had haste, unless it entered
 * the battlefield this turn (Chaos Lord). Only the summoning-sickness restriction on attacking is
 * lifted — unlike {@code GrantKeywordEffect(HASTE, …)} it does not allow {@code {T}} ability
 * activation. Read in {@code AttackLegalityService.canAttack}.
 */
public record CanAttackAsThoughHasteUnlessEnteredThisTurnEffect() implements CardEffect {
}
