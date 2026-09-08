package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;

/**
 * Lets the controller choose a matching card from a graveyard at resolution, then allows them to
 * cast that card for the rest of the turn. The graveyard choice is not a target. When
 * {@code withoutPayingManaCost} is true, the permission is a one-shot free cast.
 */
public record ChooseCardFromGraveyardAndGrantCastPermissionEffect(
        CardPredicate filter,
        GraveyardSearchScope scope,
        boolean exileInsteadOfGraveyard,
        boolean withoutPayingManaCost
) implements CardEffect {

    public ChooseCardFromGraveyardAndGrantCastPermissionEffect(
            CardPredicate filter, GraveyardSearchScope scope, boolean exileInsteadOfGraveyard) {
        this(filter, scope, exileInsteadOfGraveyard, false);
    }

    public ChooseCardFromGraveyardAndGrantCastPermissionEffect {
        Objects.requireNonNull(filter, "filter");
        Objects.requireNonNull(scope, "scope");
    }
}
