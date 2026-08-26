package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromChosenPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "FIN", collectorNumber = "222")
public class GarnetPrincessOfAlexandria extends Card {

    public GarnetPrincessOfAlexandria() {
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                SequenceEffect.of(
                        new RemoveCounterFromChosenPermanentsEffect(
                                CounterType.LORE, new PermanentHasSubtypePredicate(CardSubtype.SAGA)),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, new EventValue())),
                "Remove lore counters from any number of Sagas you control?"));
    }
}
