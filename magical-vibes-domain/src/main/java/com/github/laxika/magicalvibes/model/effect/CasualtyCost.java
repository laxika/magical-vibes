package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

/** Optional additional cost to sacrifice a creature with at least the specified power. */
public record CasualtyCost(int minimumPower, boolean powerAtLeastChosenX) implements CostEffect {

    public CasualtyCost(int minimumPower) {
        this(minimumPower, false);
    }

    /** Casualty X cost whose required creature power is at least the chosen X value (CR 702.153a). */
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
