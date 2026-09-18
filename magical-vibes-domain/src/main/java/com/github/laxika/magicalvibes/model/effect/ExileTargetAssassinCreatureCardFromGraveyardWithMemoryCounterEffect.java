package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

/** Exiles the targeted Assassin creature card and puts a memory counter on it. */
public record ExileTargetAssassinCreatureCardFromGraveyardWithMemoryCounterEffect()
        implements GraveyardCardChoosingEffect {

    public static CardPredicate targetFilter() {
        return new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardSubtypePredicate(CardSubtype.ASSASSIN)));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(
                targetFilter(), GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
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
