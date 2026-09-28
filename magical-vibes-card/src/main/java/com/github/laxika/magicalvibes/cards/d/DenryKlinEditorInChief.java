package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ChooseCounterTypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnEnteringCreatureEqualToSourceCountersEffect;

@CardRegistration(set = "NCC", collectorNumber = "71")
@CardRegistration(set = "NCC", collectorNumber = "171")
public class DenryKlinEditorInChief extends Card {

    public DenryKlinEditorInChief() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChooseCounterTypeOnEnterEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, CounterType.FIRST_STRIKE, CounterType.VIGILANCE));
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD,
                new ConditionalEffect(
                        new SourceCounterThreshold(1, CounterType.ANY),
                        new PutCountersOnEnteringCreatureEqualToSourceCountersEffect()));
    }
}
