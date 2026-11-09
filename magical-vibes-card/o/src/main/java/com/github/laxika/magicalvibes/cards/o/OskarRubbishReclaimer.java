package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DistinctManaValuesAmongCardsInGraveyard;
import com.github.laxika.magicalvibes.model.effect.CastDiscardedCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NCC", collectorNumber = "77")
@CardRegistration(set = "NCC", collectorNumber = "177")
public class OskarRubbishReclaimer extends Card {

    public OskarRubbishReclaimer() {
        addStaticEffect(new ReduceOwnCastCostEffect(new DistinctManaValuesAmongCardsInGraveyard()));
        addTrigger(Triggers.youDiscardCard().withFilter(new CardNotPredicate(new CardTypePredicate(CardType.LAND))),
                new CastDiscardedCardFromGraveyardEffect());
    }
}
