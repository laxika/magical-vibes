package com.github.laxika.magicalvibes.model.effect;

/**
 * Describes a targeted effect whose selected targets must stay within one combined mana-value
 * limit. The target-selection services use this capability to enforce the limit while targets are
 * chosen; the effect handler must also enforce it when the targets resolve.
 */
public interface AggregateManaValueTargetEffect extends CardEffect {

    int maxTotalManaValue();

    boolean hasAggregateManaValueLimit();
}
