package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CounterRemovalSubject;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterAndGainLifeEffect;

@CardRegistration(set = "MRD", collectorNumber = "249")
public class SunDroplet extends Card {

    public SunDroplet() {
        addEffect(EffectSlot.ON_CONTROLLER_DEALT_DAMAGE,
                new PutCountersOnSelfEffect(CounterType.CHARGE, new EventValue()));
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new MayEffect(
                        new RemoveCounterAndGainLifeEffect(CounterType.CHARGE, 1, CounterRemovalSubject.SOURCE),
                        "Remove a charge counter from Sun Droplet to gain 1 life?"));
    }
}
