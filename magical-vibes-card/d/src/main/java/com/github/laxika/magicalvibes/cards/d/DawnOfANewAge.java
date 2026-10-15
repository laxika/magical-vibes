package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "HOC", collectorNumber = "13")
@CardRegistration(set = "HOC", collectorNumber = "53")
@CardRegistration(set = "HOC", collectorNumber = "162")
public class DawnOfANewAge extends Card {

    public DawnOfANewAge() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithCountersEffect(
                CounterType.HOPE,
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER)));

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, SequenceEffect.of(
                new ConditionalEffect(
                        new SourceCounterThreshold(1, CounterType.HOPE),
                        SequenceEffect.of(
                                new RemoveCounterFromSourceEffect(CounterType.HOPE, 1),
                                new DrawCardEffect())),
                new ConditionalEffect(
                        new NotCondition(new SourceCounterThreshold(1, CounterType.HOPE)),
                        SequenceEffect.of(new SacrificeSelfEffect(), new GainLifeEffect(4)))));
    }
}
