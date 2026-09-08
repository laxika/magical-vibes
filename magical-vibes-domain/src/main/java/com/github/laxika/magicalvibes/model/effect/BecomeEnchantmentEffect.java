package com.github.laxika.magicalvibes.model.effect;

/** Restores the source permanent's original enchantment card form. */
public record BecomeEnchantmentEffect(boolean onlyIfCreature) implements CardEffect {

    public BecomeEnchantmentEffect() {
        this(false);
    }

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(null, false, null, true, 1);
    }
}
