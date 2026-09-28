package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

/** Exiles target creature card from a graveyard with a brain counter on it. */
public record ExileTargetCreatureCardFromGraveyardWithBrainCounterEffect() implements CardEffect {

    public static CardPredicate targetFilter() {
        return new CardTypePredicate(CardType.CREATURE);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(
                targetFilter(), GraveyardSearchScope.ALL_GRAVEYARDS));
    }
}
