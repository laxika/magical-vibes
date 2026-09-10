package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

@CardRegistration(set = "TMT", collectorNumber = "38")
@CardRegistration(set = "TMT", collectorNumber = "197")
public class DonatelloWayWithMachines extends Card {

    public DonatelloWayWithMachines() {
        addEffect(EffectSlot.ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
