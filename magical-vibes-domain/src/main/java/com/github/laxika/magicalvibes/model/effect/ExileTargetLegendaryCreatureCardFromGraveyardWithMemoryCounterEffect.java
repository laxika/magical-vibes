package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

/** Exiles up to one legendary creature card from a graveyard with a memory counter on it. */
public record ExileTargetLegendaryCreatureCardFromGraveyardWithMemoryCounterEffect()
        implements GraveyardCardChoosingEffect {

    public static CardPredicate targetFilter() {
        return new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardSupertypePredicate(CardSupertype.LEGENDARY)));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(
                targetFilter(), GraveyardSearchScope.ALL_GRAVEYARDS));
    }

    @Override
    public int graveyardChoiceMaxTargets() {
        return 1;
    }

    @Override
    public CardPredicate graveyardChoiceFilter() {
        return targetFilter();
    }

    @Override
    public boolean hasOptionalTarget() {
        return true;
    }
}
