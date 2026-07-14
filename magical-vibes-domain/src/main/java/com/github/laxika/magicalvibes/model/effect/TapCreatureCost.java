package com.github.laxika.magicalvibes.model.effect;

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
 */
public record TapCreatureCost(PermanentPredicate predicate, boolean excludeSelf,
                              boolean trackTappedCreaturePower) implements CostEffect {

    public TapCreatureCost(PermanentPredicate predicate) {
        this(predicate, false, false);
    }
}
