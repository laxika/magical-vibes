package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * "You may cast target [filter] card from [scope] graveyard" — a targeted cast offered while the
 * ability resolves, for the card's normal cost and without any later permission (Tinybones, the
 * Pickpocket). {@code anyManaType} lets mana of any type be spent to cast the spell.
 */
public record CastTargetCardFromGraveyardEffect(
        CardPredicate filter,
        GraveyardSearchScope scope,
        boolean anyManaType
) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(filter, scope));
    }
}
