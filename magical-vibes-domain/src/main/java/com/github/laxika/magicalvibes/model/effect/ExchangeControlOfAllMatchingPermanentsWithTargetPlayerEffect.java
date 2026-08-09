package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Exchanges control of all permanents matching {@code filter} controlled by the resolving
 * player and the targeted player for the specified duration.
 */
public record ExchangeControlOfAllMatchingPermanentsWithTargetPlayerEffect(
        PermanentPredicate filter, ControlDuration duration) implements CardEffect, ControlStealingEffect {

    public ExchangeControlOfAllMatchingPermanentsWithTargetPlayerEffect(PermanentPredicate filter) {
        this(filter, ControlDuration.PERMANENT);
    }

    @Override
    public ControlDuration controlDuration() {
        return duration;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
