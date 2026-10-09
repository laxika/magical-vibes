package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "PLC", collectorNumber = "37")
public class Chronozoa extends Card {

    public Chronozoa() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.TIME, new Fixed(3)));
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ConditionalEffect(new SourceCounterThreshold(1, CounterType.TIME),
                        new RemoveCounterFromSourceEffect(CounterType.TIME, 1)));
        addEffect(EffectSlot.ON_SELF_TIME_COUNTERS_REMOVED,
                new ConditionalEffect(new NotCondition(new SourceCounterThreshold(1, CounterType.TIME)),
                        new SacrificeSelfEffect()));
        addEffect(EffectSlot.ON_DEATH, new TriggeringPermanentConditionalEffect(
                new PermanentNotPredicate(new PermanentHasCountersPredicate(CounterType.TIME)),
                new CreateTokenCopyOfSourceEffect(false, 2)));
    }
}
