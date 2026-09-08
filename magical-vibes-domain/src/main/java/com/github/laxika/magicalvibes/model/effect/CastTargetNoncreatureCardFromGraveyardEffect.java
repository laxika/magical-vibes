package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

/**
 * Offers a target noncreature card with a bounded mana value from the controller's graveyard for
 * a free cast during resolution.
 */
public record CastTargetNoncreatureCardFromGraveyardEffect(int maxManaValue)
        implements GraveyardCardChoosingEffect {

    public CardPredicate cardFilter() {
        return new CardAllOfPredicate(List.of(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                new CardMaxManaValuePredicate(maxManaValue)));
    }

    @Override
    public int graveyardChoiceMaxTargets() {
        return 1;
    }

    @Override
    public boolean graveyardChoiceExactTargets() {
        return true;
    }

    @Override
    public CardPredicate graveyardChoiceFilter() {
        return cardFilter();
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(
                cardFilter(), GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
    }
}
