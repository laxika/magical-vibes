package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Global static type-changing effect (CR 613.1d layer 4): permanents matching {@code filter} lose
 * {@code supertype}. Melting ("All lands are no longer snow") is
 * {@code new PermanentsMatchingLoseSupertypeEffect(new PermanentIsLandPredicate(), CardSupertype.SNOW)}.
 *
 * <p>Symmetric — it applies to every player's permanents, not just the source controller's. The
 * removal is not accumulated into {@code StaticBonusAccumulator}; it is consulted directly by
 * {@code GameQueryService.hasEffectiveSupertype}, which every supertype check that can be affected
 * routes through.
 */
public record PermanentsMatchingLoseSupertypeEffect(PermanentPredicate filter, CardSupertype supertype)
        implements CardEffect {
}
