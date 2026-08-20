package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The controller chooses any number of matching permanents to exile until the source permanent
 * leaves the battlefield. The exiled cards then return to the battlefield under their owners'
 * control. Tokens cease to exist instead of returning.
 *
 * @param filter permanents that may be chosen
 */
public record ExileAnyNumberOfPermanentsUntilSourceLeavesEffect(PermanentPredicate filter)
        implements CardEffect {
}
