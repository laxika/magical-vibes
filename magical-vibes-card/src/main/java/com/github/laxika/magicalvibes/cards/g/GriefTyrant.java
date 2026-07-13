package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetForEachDyingSourceCounterEffect;

@CardRegistration(set = "SHM", collectorNumber = "189")
public class GriefTyrant extends Card {

    public GriefTyrant() {
        // This creature enters with four -1/-1 counters on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.MINUS_ONE_MINUS_ONE, new Fixed(4)));

        // When this creature dies, put a -1/-1 counter on target creature for each -1/-1 counter on it.
        addEffect(EffectSlot.ON_DEATH,
                new PutCounterOnTargetForEachDyingSourceCounterEffect(CounterType.MINUS_ONE_MINUS_ONE));
    }
}
