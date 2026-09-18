package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

/**
 * Returns a targeted creature card from the controller's graveyard to the battlefield when its
 * mana value is at most the evaluated amount, and to its owner's hand otherwise.
 */
public record ReturnTargetCreatureFromGraveyardToBattlefieldOrHandByManaValueEffect(
        DynamicAmount maxManaValue) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(
                new CardTypePredicate(CardType.CREATURE),
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
    }
}
