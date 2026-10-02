package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

/** Conjures a perpetual any-color duplicate of a target creature card from an opponent's graveyard into hand. */
public record ConjureDuplicateOfTargetCreatureCardFromOpponentGraveyardIntoHandEffect()
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(
                new CardTypePredicate(CardType.CREATURE), GraveyardSearchScope.OPPONENT_GRAVEYARD));
    }
}
