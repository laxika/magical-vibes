package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasUnearthPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

/** Perpetually grants unearth, with a cost equal to the target card's mana cost. */
public record PerpetuallyGrantUnearthToTargetCreatureCardEffect() implements CardEffect {

    public static CardPredicate targetFilter() {
        return new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardNotPredicate(new CardHasUnearthPredicate())));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(
                targetFilter(), GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
    }
}
