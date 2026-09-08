package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Each player sacrifices 1/divisor of the permanents they control matching {@code filter},
 * rounded down, chosen by that player.
 */
public record EachPlayerSacrificesFractionRoundedDownEffect(int divisor, PermanentPredicate filter)
        implements CardEffect {
}
