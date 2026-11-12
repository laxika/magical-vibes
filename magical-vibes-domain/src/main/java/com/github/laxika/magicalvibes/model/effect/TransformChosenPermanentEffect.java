package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * During resolution, the controller chooses one matching permanent and transforms it.
 * The choice is not a target choice.
 *
 * @param filter the permanents to choose among, or {@code null} for the permanents an earlier step of the resolution
 *               picked ("put it onto the battlefield. You may transform it")
 */
public record TransformChosenPermanentEffect(PermanentPredicate filter) implements CardEffect {

    /** "Transform it": the permanent an earlier step of the resolution picked (the resolving entry's picks). */
    public static TransformChosenPermanentEffect pickedPermanent() {
        return new TransformChosenPermanentEffect(null);
    }
}
