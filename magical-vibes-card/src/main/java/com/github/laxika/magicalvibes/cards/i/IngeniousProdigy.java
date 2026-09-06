package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceThenEffect;

@CardRegistration(set = "WOE", collectorNumber = "56")
public class IngeniousProdigy extends Card {

    public IngeniousProdigy() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue()));

        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ConditionalEffect(
                        new SourceCounterThreshold(1, CounterType.PLUS_ONE_PLUS_ONE),
                        new MayEffect(
                                new RemoveCounterFromSourceThenEffect(
                                        CounterType.PLUS_ONE_PLUS_ONE, new DrawCardEffect()),
                                "Remove a +1/+1 counter from Ingenious Prodigy?")));
    }
}
