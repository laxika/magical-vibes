package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

/**
 * Conditional block-trigger descriptor: "Whenever this creature blocks a creature with [keyword],
 * this creature gets +X/+Y until end of turn."
 * <p>
 * Place in {@code ON_BLOCK} slot. {@code CombatService} checks the required keyword on the attacker
 * at trigger time and converts to {@link BoostSelfEffect} if the condition is met.
 */
public record BoostSelfWhenBlockingKeywordEffect(Keyword requiredKeyword, int powerBoost, int toughnessBoost) implements CardEffect {
}
