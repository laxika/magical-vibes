package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

@CardRegistration(set = "MSC", collectorNumber = "624")
public class ImperialCosmographer extends Card {

    public ImperialCosmographer() {
        addEffect(EffectSlot.ON_ALLY_CREATURES_LEAVE_BATTLEFIELD_WITHOUT_DYING,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2));
    }
}
