package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

/** Destroys target enchantment and every other enchantment sharing an effective color with it. */
public record DestroyTargetAndOtherEnchantmentsSharingColorEffect() implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent(), new PermanentIsEnchantmentPredicate());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.DESTROY;
    }
}
