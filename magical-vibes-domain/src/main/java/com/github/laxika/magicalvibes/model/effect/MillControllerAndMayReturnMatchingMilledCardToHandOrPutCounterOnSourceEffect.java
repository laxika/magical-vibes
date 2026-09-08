package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Mills cards from the controller's library, then offers matching milled cards one at a time for
 * return to hand, up to {@code maxCount} cards. If every offer is declined, a +1/+1 counter is
 * put on the source permanent.
 */
public record MillControllerAndMayReturnMatchingMilledCardToHandOrPutCounterOnSourceEffect(
        DynamicAmount count, CardPredicate filter, DynamicAmount maxCount)
        implements CombatDamageAmountAwareEffect, CombatDamageTriggerContextEffect {

    public MillControllerAndMayReturnMatchingMilledCardToHandOrPutCounterOnSourceEffect(
            int count, CardPredicate filter) {
        this(new Fixed(count), filter, new Fixed(1));
    }

    public MillControllerAndMayReturnMatchingMilledCardToHandOrPutCounterOnSourceEffect(
            DynamicAmount count, CardPredicate filter) {
        this(count, filter, new Fixed(1));
    }

    public MillControllerAndMayReturnMatchingMilledCardToHandOrPutCounterOnSourceEffect {
        if (count == null || filter == null || maxCount == null) {
            throw new NullPointerException("count, filter, and maxCount are required");
        }
    }

    @Override
    public DynamicAmount combatDamageAmount() {
        return count;
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.SOURCE_SELF;
    }
}
