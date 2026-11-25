package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Cost: sacrifice {@code count} permanents matching {@code filter}, chosen by the payer and sacrificed together.
 *
 * @param count       how many permanents
 * @param filter      the permanents that may be sacrificed
 * @param description the plural noun prompts and log lines use ("nonland permanents"), or {@code null} for
 *                    "permanents"
 */
public record SacrificeMultiplePermanentsCost(int count, PermanentPredicate filter, String description)
        implements CostEffect {

    public SacrificeMultiplePermanentsCost(int count, PermanentPredicate filter) {
        this(count, filter, null);
    }

    @Override
    public PermanentPredicate consumedPermanentFilter() {
        return filter;
    }

    @Override
    public boolean sacrificesChosenPermanent() {
        return true;
    }
}
