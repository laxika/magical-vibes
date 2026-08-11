package com.github.laxika.magicalvibes.model.effect;

/** Restores the source permanent's original enchantment card form. */
public record BecomeEnchantmentEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(null, false, null, true, 1);
    }
}
