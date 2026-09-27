package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Chooses one matching permanent controlled by the resolving player, then animates it. */
public record AnimateChosenOwnPermanentEffect(PermanentPredicate filter,
                                              AnimatePermanentsEffect animation) implements CardEffect {
}
