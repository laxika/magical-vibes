package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: each opponent must attack with at least one creature each combat if able.
 * The opponent may choose any legal attack target.
 */
public record OpponentsMustAttackEffect() implements OpponentsMustAttackRequirementEffect {
}
