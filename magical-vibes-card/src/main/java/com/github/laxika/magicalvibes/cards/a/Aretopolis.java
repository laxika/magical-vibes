package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PlaneswalkIfPlanarSourceHasCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "OPC2", collectorNumber = "10")
public class Aretopolis extends Card {

    public Aretopolis() {
        addEffect(EffectSlot.PLANESWALK_TO_TRIGGERED, SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.SCROLL),
                new GainLifeEffect(new CountersOnSource(CounterType.SCROLL)),
                new PlaneswalkIfPlanarSourceHasCountersEffect(CounterType.SCROLL, 10)));
        addEffect(EffectSlot.UPKEEP_TRIGGERED, SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.SCROLL),
                new GainLifeEffect(new CountersOnSource(CounterType.SCROLL)),
                new PlaneswalkIfPlanarSourceHasCountersEffect(CounterType.SCROLL, 10)));
        addEffect(EffectSlot.CHAOS_TRIGGERED, SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.SCROLL),
                new DrawCardEffect(new CountersOnSource(CounterType.SCROLL)),
                new PlaneswalkIfPlanarSourceHasCountersEffect(CounterType.SCROLL, 10)));
    }
}
