package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

/** Exiles up to one creature card from any graveyard and conjures a modified duplicate. */
public record ExileTargetCreatureCardThenConjureSkeletonDuplicateEffect()
        implements CardEffect, OptionalTargetEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(
                new CardTypePredicate(CardType.CREATURE),
                GraveyardSearchScope.ALL_GRAVEYARDS));
    }

    @Override
    public boolean hasOptionalTarget() {
        return true;
    }
}
