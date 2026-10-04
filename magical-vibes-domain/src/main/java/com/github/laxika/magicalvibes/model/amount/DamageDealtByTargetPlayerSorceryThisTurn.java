package com.github.laxika.magicalvibes.model.amount;

/**
 * Total damage dealt by the chosen sorcery spell cast by the chosen player this turn.
 * The spell's id comes from the stack entry's chosen-permanent channel.
 */
public record DamageDealtByTargetPlayerSorceryThisTurn() implements DynamicAmount {
}
