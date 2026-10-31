package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReturnedPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "EOE", collectorNumber = "33")
public class ScoutForSurvivors extends Card {

    public ScoutForSurvivors() {
        addSpellEffect(new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                new CardTypePredicate(CardType.CREATURE), 3, 3));
        addSpellEffect(new PutCounterOnReturnedPermanentsEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));
    }
}
