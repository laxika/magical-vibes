package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

public record SacrificeDistinctNamePermanentsCost(int count, PermanentPredicate filter) implements CostEffect {

    @Override
    public PermanentPredicate consumedPermanentFilter() {
        return filter;
    }

    @Override
    public boolean sacrificesChosenPermanent() {
        return true;
    }
}
