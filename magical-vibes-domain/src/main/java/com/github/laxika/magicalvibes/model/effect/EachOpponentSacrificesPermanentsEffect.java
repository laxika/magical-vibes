package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Each opponent sacrifices N permanents matching the given filter.
 * Opponents choose which permanents to sacrifice in APNAP order.
 * If an opponent controls fewer matching permanents than the count, they sacrifice all of them.
 *
 * <p>Example: "Each opponent sacrifices a land." →
 * {@code new EachOpponentSacrificesPermanentsEffect(1, new PermanentIsLandPredicate())}
 */
public record EachOpponentSacrificesPermanentsEffect(
        int count,
        PermanentPredicate filter
) implements CardEffect {
}
