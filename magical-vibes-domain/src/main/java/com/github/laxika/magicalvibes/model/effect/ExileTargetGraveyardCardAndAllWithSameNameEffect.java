package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

/** Exiles a target nonland card from a graveyard and all permanents and graveyard cards with its name. */
public record ExileTargetGraveyardCardAndAllWithSameNameEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(
                new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                GraveyardSearchScope.ALL_GRAVEYARDS));
    }
}
