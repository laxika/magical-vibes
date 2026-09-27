package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

/**
 * Targets a card in the controller's graveyard and returns it to the battlefield when its mana
 * value is at most the controller's experience total, otherwise returning it to that player's hand.
 */
public record ReturnTargetCardFromGraveyardByExperienceEffect(CardPredicate filter) implements CardEffect {

    public ReturnTargetCardFromGraveyardByExperienceEffect() {
        this(new CardTypePredicate(CardType.CREATURE));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(
                filter, GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
    }
}
