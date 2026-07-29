package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.DrawPerSourceCounterThenDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

@CardRegistration(set = "MIR", collectorNumber = "272")
public class MalignantGrowth extends Card {

    public MalignantGrowth() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CumulativeUpkeepEffect("{1}"));
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new PutCountersOnSelfEffect(CounterType.GROWTH));
        addEffect(EffectSlot.EACH_DRAW_TRIGGERED, new DrawPerSourceCounterThenDamageEffect(CounterType.GROWTH));
    }
}
