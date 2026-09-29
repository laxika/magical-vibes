package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Resolves to an attack tax affecting creatures attacking the ability controller until that player's next turn. */
public record RequirePaymentToAttackControllerUntilNextTurnEffect(DynamicAmount amountPerAttacker)
        implements CardEffect {

    public RequirePaymentToAttackControllerUntilNextTurnEffect(int amountPerAttacker) {
        this(new Fixed(amountPerAttacker));
    }
}
