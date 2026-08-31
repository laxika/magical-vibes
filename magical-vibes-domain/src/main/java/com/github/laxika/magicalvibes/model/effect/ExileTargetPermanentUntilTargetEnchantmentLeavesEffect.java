package com.github.laxika.magicalvibes.model.effect;

/**
 * Exile the first target until the second target enchantment leaves the battlefield.
 *
 * <p>This is a two-target effect: the first target is the permanent that is exiled and the
 * second target is the enchantment that defines the duration. The ability supplying this effect
 * is responsible for declaring the per-position target restrictions.</p>
 */
public record ExileTargetPermanentUntilTargetEnchantmentLeavesEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent());
    }
}
