package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

/** Optional additional cost to sacrifice a creature with at least the specified power. */
public record CasualtyCost(int minimumPower, boolean powerMustEqualChosenX) implements CostEffect {

    public CasualtyCost(int minimumPower) {
        this(minimumPower, false);
    }

    /** Casualty cost whose required creature power is the chosen X value. */
    public static CasualtyCost matchingChosenX() {
        return new CasualtyCost(0, true);
    }

    @Override
    public PermanentPredicate consumedPermanentFilter() {
        return new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentPowerAtLeastPredicate(minimumPower)));
    }

    @Override
    public boolean sacrificesChosenCreature() {
        return true;
    }

    @Override
    public boolean tracksSacrificedCard() {
        return true;
    }
}
