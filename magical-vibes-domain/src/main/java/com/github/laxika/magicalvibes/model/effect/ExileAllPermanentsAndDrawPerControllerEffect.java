package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Exiles all matching permanents, then makes each permanent's controller draw one card for each
 * permanent exiled that way.
 */
public record ExileAllPermanentsAndDrawPerControllerEffect(PermanentPredicate filter) implements CardEffect {
}
