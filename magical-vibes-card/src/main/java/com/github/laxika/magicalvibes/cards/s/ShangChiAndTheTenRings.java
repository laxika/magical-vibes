package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "MSC", collectorNumber = "94")
@CardRegistration(set = "MSC", collectorNumber = "415")
public class ShangChiAndTheTenRings extends Card {

    public ShangChiAndTheTenRings() {
        // Whenever you draw a card, put a +1/+1 counter on Shang-Chi.
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));

        // When the tenth +1/+1 counter is put on Shang-Chi, draw five cards and gain 5 life.
        addEffect(EffectSlot.ON_SELF_COUNTERS_PUT, new ConditionalEffect(
                new SourceCounterThreshold(10, CounterType.PLUS_ONE_PLUS_ONE),
                SequenceEffect.of(new DrawCardEffect(5), new GainLifeEffect(5))));
    }
}
