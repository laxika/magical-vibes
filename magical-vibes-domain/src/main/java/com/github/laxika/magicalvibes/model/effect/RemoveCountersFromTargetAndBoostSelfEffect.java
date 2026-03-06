package com.github.laxika.magicalvibes.model.effect;

/**
 * Remove up to X counters of any type from target permanent, then boost source creature
 * +1/+0 until end of turn for each counter actually removed. X comes from the stack entry's xValue.
 */
public record RemoveCountersFromTargetAndBoostSelfEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
