package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

/** Returns any number of target creature cards from the controller's graveyard within a power cap. */
public record ReturnTargetCreatureCardsFromGraveyardToBattlefieldWithTotalPowerEffect(int maxTotalPower)
        implements GraveyardCardChoosingEffect {

    private static final CardPredicate CREATURE_CARD = new CardTypePredicate(CardType.CREATURE);

    public ReturnTargetCreatureCardsFromGraveyardToBattlefieldWithTotalPowerEffect {
        if (maxTotalPower < 0) {
            throw new IllegalArgumentException("maxTotalPower cannot be negative");
        }
    }

    @Override
    public int graveyardChoiceMaxTargets() {
        return Integer.MAX_VALUE;
    }

    @Override
    public CardPredicate graveyardChoiceFilter() {
        return CREATURE_CARD;
    }

    @Override
    public Integer graveyardChoiceMaxTotalPower() {
        return maxTotalPower;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(
                CREATURE_CARD, GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
    }
}
