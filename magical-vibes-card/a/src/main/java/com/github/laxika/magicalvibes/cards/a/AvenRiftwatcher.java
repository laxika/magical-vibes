package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.condition.NotCondition;

@CardRegistration(set = "PLC", collectorNumber = "1")
@CardRegistration(set = "EMA", collectorNumber = "1")
@CardRegistration(set = "TSR", collectorNumber = "6")
public class AvenRiftwatcher extends Card {

    public AvenRiftwatcher() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.TIME, new Fixed(3)));
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ConditionalEffect(new SourceCounterThreshold(1, CounterType.TIME),
                        new RemoveCounterFromSourceEffect(CounterType.TIME, 1)));
        addEffect(EffectSlot.ON_SELF_TIME_COUNTERS_REMOVED,
                new ConditionalEffect(new NotCondition(new SourceCounterThreshold(1, CounterType.TIME)),
                        new SacrificeSelfEffect()));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(2));
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new GainLifeEffect(2));
    }
}
