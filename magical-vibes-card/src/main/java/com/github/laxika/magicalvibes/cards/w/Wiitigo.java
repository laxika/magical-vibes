package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutOrRemoveCounterIfBlockedSinceLastUpkeepEffect;

@CardRegistration(set = "ICE", collectorNumber = "276")
public class Wiitigo extends Card {

    public Wiitigo() {
        // "This creature enters with six +1/+1 counters on it."
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(6)));

        // "At the beginning of your upkeep, put a +1/+1 counter on this creature if it has blocked
        //  or been blocked since your last upkeep. Otherwise, remove a +1/+1 counter from it."
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new PutOrRemoveCounterIfBlockedSinceLastUpkeepEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
