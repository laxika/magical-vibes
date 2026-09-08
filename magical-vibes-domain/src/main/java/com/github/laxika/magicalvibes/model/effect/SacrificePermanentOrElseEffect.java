package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The active player sacrifices a matching permanent if they can. If they cannot, the alternate
 * effect resolves instead.
 */
public record SacrificePermanentOrElseEffect(
        PermanentPredicate filter,
        CardEffect elseEffect,
        String permanentDescription
) implements CardEffect {
}
