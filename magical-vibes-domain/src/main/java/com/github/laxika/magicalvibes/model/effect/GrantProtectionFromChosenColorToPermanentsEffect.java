package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * STATIC: permanents matching {@code filter} have protection from the color chosen for the
 * source permanent as it entered the battlefield.
 */
public record GrantProtectionFromChosenColorToPermanentsEffect(PermanentPredicate filter)
        implements CardEffect {
}
