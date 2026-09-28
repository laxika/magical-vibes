package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Describes a targeted effect whose selected targets must stay within one combined mana-value
 * limit. The target-selection services use this capability to enforce the limit while targets are
 * chosen; the effect handler must also enforce it when the targets resolve.
 */
public interface AggregateManaValueTargetEffect extends CardEffect {

    int maxTotalManaValue();

    /**
     * Returns a dynamic aggregate limit when the cap is evaluated from the resolving entry
     * (for example, the X paid to cast the source spell). Fixed-limit effects return {@code null}.
     */
    default DynamicAmount dynamicMaxTotalManaValue() {
        return null;
    }

    boolean hasAggregateManaValueLimit();
}
