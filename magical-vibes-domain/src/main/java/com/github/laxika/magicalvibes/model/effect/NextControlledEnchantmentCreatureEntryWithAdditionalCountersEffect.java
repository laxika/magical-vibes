package com.github.laxika.magicalvibes.model.effect;

/**
 * Registers a one-shot replacement for the next entry event this turn in which one or more
 * enchantment creatures enter under the effect controller's control.
 */
public record NextControlledEnchantmentCreatureEntryWithAdditionalCountersEffect(int count)
        implements CardEffect {
}
