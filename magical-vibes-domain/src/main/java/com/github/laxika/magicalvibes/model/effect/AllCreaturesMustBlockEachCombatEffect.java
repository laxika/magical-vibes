package com.github.laxika.magicalvibes.model.effect;

/**
 * STATIC global requirement: every creature that can legally block must block each combat.
 */
public record AllCreaturesMustBlockEachCombatEffect() implements GlobalMustBlockEachCombatEffect {
}
