package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Creates the configured token, mills one card from the controller's library, and repeats the
 * whole process while the milled card matches the predicate. The effect is non-targeting and is
 * intended for oracle text such as Grist, the Hunger Tide's +1 ability.
 */
public record CreateTokenThenMillAndRepeatIfMilledEffect(
        CreateTokenEffect tokenEffect,
        CardPredicate repeatFilter
) implements CardEffect {
}
