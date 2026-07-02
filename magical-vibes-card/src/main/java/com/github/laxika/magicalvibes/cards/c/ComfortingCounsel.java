package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "SOS", collectorNumber = "143")
public class ComfortingCounsel extends Card {

    public ComfortingCounsel() {
        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE, new PutCountersOnSelfEffect(CounterType.GROWTH));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new SourceCounterThreshold(5, CounterType.GROWTH), new StaticBoostEffect(3, 3, GrantScope.OWN_CREATURES)
        ));
    }
}
