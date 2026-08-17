package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;

/**
 * Lets the controller choose a matching card from a graveyard at resolution, then allows them to
 * cast that card for the rest of the turn. The graveyard choice is not a target.
 */
public record ChooseCardFromGraveyardAndGrantCastPermissionEffect(
        CardPredicate filter,
        GraveyardSearchScope scope,
        boolean exileInsteadOfGraveyard
) implements CardEffect {

    public ChooseCardFromGraveyardAndGrantCastPermissionEffect {
        Objects.requireNonNull(filter, "filter");
        Objects.requireNonNull(scope, "scope");
    }
}
