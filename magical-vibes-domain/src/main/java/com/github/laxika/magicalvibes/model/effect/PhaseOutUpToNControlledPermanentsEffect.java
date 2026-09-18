package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** The controller chooses up to the evaluated number of matching permanents they control to phase out. */
public record PhaseOutUpToNControlledPermanentsEffect(
        DynamicAmount maxCount, PermanentPredicate filter) implements CardEffect {
}
