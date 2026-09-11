package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;

/**
 * During resolution, lets the controller choose and may cast one matching spell from a graveyard
 * using its normal mana cost.
 */
public record CastCardFromGraveyardEffect(
        CardPredicate filter,
        GraveyardSearchScope scope,
        CardPredicate exileInsteadOfGraveyardFilter,
        boolean allowAdventure
) implements CardEffect {

    public CastCardFromGraveyardEffect {
        Objects.requireNonNull(filter, "filter");
        Objects.requireNonNull(scope, "scope");
        Objects.requireNonNull(exileInsteadOfGraveyardFilter, "exileInsteadOfGraveyardFilter");
    }
}
