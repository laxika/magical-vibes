package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "5DN", collectorNumber = "109")
public class ClearwaterGoblet extends Card {

    public ClearwaterGoblet() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.CHARGE, new XValue()));
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new MayEffect(
                        new GainLifeEffect(new CountersOnSource(CounterType.CHARGE)),
                        "Gain life equal to the number of charge counters on Clearwater Goblet?"));
    }
}
