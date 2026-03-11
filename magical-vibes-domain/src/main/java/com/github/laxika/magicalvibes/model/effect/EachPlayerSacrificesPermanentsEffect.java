package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Each player sacrifices N permanents matching the given filter.
 * Players choose which permanents to sacrifice in APNAP order.
 * If a player controls fewer matching permanents than the count, they sacrifice all of them.
 *
 * <p>Example: "Each player sacrifices five lands." →
 * {@code new EachPlayerSacrificesPermanentsEffect(5, new PermanentIsLandPredicate())}
 */
public record EachPlayerSacrificesPermanentsEffect(
        int count,
        PermanentPredicate filter
) implements CardEffect {
}
