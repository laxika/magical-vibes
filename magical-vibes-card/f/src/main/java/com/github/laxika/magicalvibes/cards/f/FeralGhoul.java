package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GiveEachOpponentRadCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

@CardRegistration(set = "PIP", collectorNumber = "44")
@CardRegistration(set = "PIP", collectorNumber = "572")
@CardRegistration(set = "PIP", collectorNumber = "381")
@CardRegistration(set = "PIP", collectorNumber = "909")
public class FeralGhoul extends Card {

    public FeralGhoul() {
        // Whenever another creature you control dies, put a +1/+1 counter on this creature.
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));

        // When this creature dies, each opponent gets rad counters equal to its power.
        addEffect(EffectSlot.ON_DEATH,
                new GiveEachOpponentRadCountersEffect(new EventValue()));
    }
}
