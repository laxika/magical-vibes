package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Resolves to a life attack tax affecting creatures attacking the ability controller until that player's next turn. */
public record RequireLifePaymentToAttackControllerUntilNextTurnEffect(DynamicAmount lifeCostPerAttacker)
        implements CardEffect {

    public RequireLifePaymentToAttackControllerUntilNextTurnEffect(int lifeCostPerAttacker) {
        this(new Fixed(lifeCostPerAttacker));
    }
}
