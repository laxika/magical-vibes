package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

/**
 * Optional land casualty replacement: the controller may sacrifice a creature with at least the
 * specified power as the land enters. If they do, a copy of the land is created by its ETB effect.
 */
public record LandCasualtyEffect(int minimumPower) implements EntryCostReplacementEffect {

    public LandCasualtyEffect {
        if (minimumPower < 0) {
            throw new IllegalArgumentException("minimumPower must not be negative");
        }
    }

    @Override
    public Kind kind() {
        return Kind.LAND_CASUALTY;
    }

    @Override
    public int count() {
        return 1;
    }

    @Override
    public String description() {
        return "a creature with power " + minimumPower + " or greater";
    }

    @Override
    public PermanentPredicate permanentFilter() {
        return new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentPowerAtLeastPredicate(minimumPower)));
    }
}
