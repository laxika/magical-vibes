package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Each player sacrifices 1/divisor of the permanents they control matching {@code filter}, rounded
 * down, chosen by that player. The count is recomputed per player against their own matching
 * permanents, so each player sacrifices a different number.
 */
public record EachPlayerSacrificesFractionRoundedDownEffect(int divisor, PermanentPredicate filter)
        implements CardEffect {
}
