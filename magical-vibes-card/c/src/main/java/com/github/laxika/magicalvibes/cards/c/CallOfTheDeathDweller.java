package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnOneTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "IKO", collectorNumber = "78")
public class CallOfTheDeathDweller extends Card {

    public CallOfTheDeathDweller() {
        addSpellEffect(new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                new CardTypePredicate(CardType.CREATURE), 2, 3));
        addSpellEffect(new PutCounterOnOneTargetEffect(CounterType.DEATHTOUCH));
        addSpellEffect(new PutCounterOnOneTargetEffect(CounterType.MENACE));
    }
}
