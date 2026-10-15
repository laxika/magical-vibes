package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "FIC", collectorNumber = "51")
@CardRegistration(set = "FIC", collectorNumber = "149")
public class SiegfriedFamedSwordsman extends Card {

    public SiegfriedFamedSwordsman() {
        // When Siegfried enters, mill three cards, then put twice the number of creature cards in
        // your graveyard as +1/+1 counters on Siegfried.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SequenceEffect.of(
                new MillEffect(3, MillRecipient.CONTROLLER),
                new PutCountersOnSelfEffect(
                        CounterType.PLUS_ONE_PLUS_ONE,
                        new Scaled(new CardsInGraveyard(
                                new CardTypePredicate(CardType.CREATURE), CountScope.CONTROLLER), 2))));
    }
}
