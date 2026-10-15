package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;

@CardRegistration(set = "WTH", collectorNumber = "117")
public class Aboroth extends Card {

    public Aboroth() {
        // Cumulative upkeep—Put a -1/-1 counter on this creature.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                CumulativeUpkeepEffect.putCounterOnSelf(CounterType.MINUS_ONE_MINUS_ONE));
    }
}
