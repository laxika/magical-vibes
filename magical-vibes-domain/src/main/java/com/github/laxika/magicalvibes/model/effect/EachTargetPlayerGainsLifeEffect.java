package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Each targeted player gains {@code amount} life. Uses {@code entry.getTargetIds()} for the target
 * list and evaluates the amount once at resolution. Pair with {@code setMinTargets(0)} and
 * {@code setMaxTargets(99)} for "any number of target players". Used by Hunters' Feast and Reward
 * the Faithful.
 */
public record EachTargetPlayerGainsLifeEffect(DynamicAmount amount) implements LifeGainEffect {

    public EachTargetPlayerGainsLifeEffect(int amount) {
        this(new Fixed(amount));
    }

    @Override
    public DynamicAmount lifeGainAmount() {
        return amount;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
