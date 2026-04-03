package com.github.laxika.magicalvibes.model.effect;

/**
 * Puts X +1/+1 counters on target permanent, where X comes from the X cost
 * paid when the spell or ability was activated (via {@code StackEntry.getXValue()}).
 */
public record PutXPlusOnePlusOneCountersOnTargetPermanentEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() { return true; }
}
