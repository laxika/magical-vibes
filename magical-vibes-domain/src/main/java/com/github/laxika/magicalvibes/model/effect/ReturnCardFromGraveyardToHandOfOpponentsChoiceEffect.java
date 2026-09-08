package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Returns a card from the controller's graveyard to their hand after an opponent chooses the
 * target. The target is still selected as the triggered ability is put on the stack; this effect
 * only changes which player makes that target choice.
 */
public record ReturnCardFromGraveyardToHandOfOpponentsChoiceEffect(CardPredicate filter)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(
                filter, GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
    }
}
