package com.github.laxika.magicalvibes.model.amount;

/**
 * The greatest total damage dealt by one sorcery spell cast by the targeted player this turn.
 * The target player's id comes from the stack entry's target channel.
 */
public record DamageDealtByTargetPlayerSorceryThisTurn() implements DynamicAmount {
}
