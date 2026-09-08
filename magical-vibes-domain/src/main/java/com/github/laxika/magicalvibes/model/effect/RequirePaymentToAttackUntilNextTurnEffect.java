package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Resolves to a defender-scoped attack tax lasting until the controller's next turn.
 */
public record RequirePaymentToAttackUntilNextTurnEffect(DynamicAmount amountPerAttacker)
        implements CardEffect {

    public RequirePaymentToAttackUntilNextTurnEffect(int amountPerAttacker) {
        this(new Fixed(amountPerAttacker));
    }
}
