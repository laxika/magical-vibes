package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

/** Exiles a targeted creature card and queues a reflexive ability using its toughness as X. */
public record ExileTargetCreatureCardFromGraveyardThenReflexiveEffect(CardEffect reflexiveEffect)
        implements CardEffect {

    public ExileTargetCreatureCardFromGraveyardThenReflexiveEffect {
        if (reflexiveEffect == null) {
            throw new IllegalArgumentException("ExileTargetCreatureCardFromGraveyardThenReflexiveEffect requires a reflexive effect");
        }
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(
                new CardTypePredicate(CardType.CREATURE), GraveyardSearchScope.ALL_GRAVEYARDS));
    }
}
