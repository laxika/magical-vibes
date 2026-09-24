package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "Tap an untapped creature you control matching {@code predicate}" as an activated-ability cost.
 *
 * @param predicate                matched (in addition to being an untapped creature you control)
 * @param excludeSelf              when {@code true}, the source permanent cannot be tapped to pay
 *                                 this cost ("other than this creature", CR 602-style)
 * @param trackTappedCreaturePower when {@code true}, the tapped creature is remembered as the
 *                                 activation's chosen permanent so a companion effect can read its
 *                                 power at resolution via {@code ChosenPermanentPower} (Impelled Giant)
 * @param trackTappedCreatureForSourceAbility when {@code true}, the tapped creature is remembered
 *                                            on the source for an ability that refers to creatures
 *                                            tapped to pay for its abilities
 * @param stationCost                       whether this cost is the station cost of a Spacecraft
 */
public record TapCreatureCost(PermanentPredicate predicate, boolean excludeSelf,
                              boolean trackTappedCreaturePower,
                              boolean trackTappedCreatureForSourceAbility,
                              boolean stationCost) implements CostEffect {

    private static final DynamicAmount ONE = new Fixed(1);

    public TapCreatureCost(PermanentPredicate predicate) {
        this(predicate, false, false, false, false);
    }

    public TapCreatureCost(PermanentPredicate predicate, boolean excludeSelf,
                           boolean trackTappedCreaturePower) {
        this(predicate, excludeSelf, trackTappedCreaturePower, false, false);
    }

    public TapCreatureCost(PermanentPredicate predicate, boolean excludeSelf,
                           boolean trackTappedCreaturePower,
                           boolean trackTappedCreatureForSourceAbility) {
        this(predicate, excludeSelf, trackTappedCreaturePower,
                trackTappedCreatureForSourceAbility, false);
    }

    @Override
    public PermanentPredicate consumedPermanentFilter() {
        return predicate;
    }

    @Override
    public DynamicAmount tappedPermanentCount() {
        return ONE;
    }

    @Override
    public boolean tappedPermanentMustBeCreature() {
        return true;
    }

    @Override
    public boolean excludesSourceFromConsumedPermanents() {
        return excludeSelf;
    }
}
