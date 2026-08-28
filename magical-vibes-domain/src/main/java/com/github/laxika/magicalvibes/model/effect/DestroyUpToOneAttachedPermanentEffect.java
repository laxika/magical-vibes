package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * At resolution, lets the controller destroy up to one permanent attached to the targeted
 * permanent that matches {@code attachedFilter}.
 *
 * @param attachedFilter predicate over the attached permanents
 */
public record DestroyUpToOneAttachedPermanentEffect(PermanentPredicate attachedFilter)
        implements CardEffect {
}
