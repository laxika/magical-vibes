package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

/**
 * Exiles a target nonland card from the controller's graveyard, then searches that player's
 * library for any number of cards with the exiled card's name and puts the chosen cards into hand.
 */
public record ExileTargetNonlandCardFromGraveyardAndSearchSameNameEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(
                new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
    }
}
