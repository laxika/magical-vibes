package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.List;

/** Turns the targeted land into an earthbended land and installs its return triggers. */
public record EarthbendTargetLandEffect(DynamicAmount counterCount) implements CardEffect {

    public EarthbendTargetLandEffect(int counterCount) {
        this(new Fixed(counterCount));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(
                TargetPredicates.land(),
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsLandPredicate(),
                        new PermanentControlledBySourceControllerPredicate())));
    }
}
