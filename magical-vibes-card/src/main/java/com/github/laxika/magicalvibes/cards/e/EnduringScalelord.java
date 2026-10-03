package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

@CardRegistration(set = "DTK", collectorNumber = "222")
@CardRegistration(set = "MOC", collectorNumber = "325")
@CardRegistration(set = "MIC", collectorNumber = "149")
@CardRegistration(set = "C16", collectorNumber = "196")
public class EnduringScalelord extends Card {

    public EnduringScalelord() {
        addEffect(EffectSlot.ON_ALLY_PLUS_ONE_PLUS_ONE_COUNTERS_PUT_ON_ANOTHER_CREATURE,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
