package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * During resolution, the controller chooses one matching permanent and transforms it.
 * The choice is not a target choice.
 */
public record TransformChosenPermanentEffect(PermanentPredicate filter) implements CardEffect {
}
