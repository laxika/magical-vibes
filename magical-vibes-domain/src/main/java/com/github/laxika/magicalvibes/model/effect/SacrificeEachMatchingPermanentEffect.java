package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "Each [filter] permanent is sacrificed by its controller." Sweeps every battlefield at resolution
 * and sacrifices each matching permanent on behalf of its own controller — no choice is offered and
 * no target is taken, so it is the sacrifice analog of {@code DestroyAllPermanentsEffect} rather
 * than of {@code SacrificePermanentsEffect} (which makes a player choose N of their own).
 *
 * @param filter which permanents are sacrificed
 */
public record SacrificeEachMatchingPermanentEffect(PermanentPredicate filter) implements CardEffect {
}
