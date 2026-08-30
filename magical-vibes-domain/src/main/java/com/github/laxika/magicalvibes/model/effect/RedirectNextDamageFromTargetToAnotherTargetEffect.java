package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Redirects the next amount of damage dealt to one target this turn to another target instead.
 * The two target groups are independent so the protected object and the destination can be any
 * combination of creature, planeswalker, and player.
 */
public record RedirectNextDamageFromTargetToAnotherTargetEffect(
        DynamicAmount amount,
        int protectedTargetGroup,
        int redirectTargetGroup
) implements CardEffect {

    public RedirectNextDamageFromTargetToAnotherTargetEffect(DynamicAmount amount) {
        this(amount, 0, 1);
    }

    public RedirectNextDamageFromTargetToAnotherTargetEffect(int amount) {
        this(new Fixed(amount));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.anyTarget());
    }
}
