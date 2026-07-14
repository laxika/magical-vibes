package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Cost effect that sacrifices X permanents matching the given filter, where X is the xValue chosen
 * by the player at activation time. The sacrifice-analog of {@link TapXPermanentsCost}: unlike
 * {@link SacrificeMultiplePermanentsCost}, the count is dynamic.
 *
 * <p>Example: "Sacrifice X Goats" (Springjack Pasture). The number sacrificed becomes the X used by
 * the ability's remaining effects (e.g. {@link AwardXAnyColorManaEffect} and a gain-X-life rider).
 */
public record SacrificeXPermanentsCost(PermanentPredicate filter) implements CostEffect {
}
