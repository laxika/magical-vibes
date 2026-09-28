package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GiveEachPlayerRadCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SLD", collectorNumber = "2455")
public class TheWiseMothman extends Card {

    public TheWiseMothman() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GiveEachPlayerRadCountersEffect(1));
        addEffect(EffectSlot.ON_ATTACK, new GiveEachPlayerRadCountersEffect(1));
        targetUpTo(new EventValue(), TargetFilters.creature(), 100)
                .addEffect(EffectSlot.ON_ANY_NONLAND_CARDS_MILLED,
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
