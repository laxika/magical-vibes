package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Schedules the controller's creatures matching the filter for sacrifice at the beginning of the
 * next end step. The matching permanents are captured when this effect resolves.
 *
 * @param filter which of the controller's creatures are scheduled
 */
public record SacrificeOwnCreaturesAtEndStepEffect(PermanentPredicate filter) implements CardEffect {
}
