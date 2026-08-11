package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;

@CardRegistration(set = "ECL", collectorNumber = "110")
public class Moonshadow extends Card {

    public Moonshadow() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.MINUS_ONE_MINUS_ONE, new Fixed(6)));

        addEffect(EffectSlot.ON_ALLY_PERMANENT_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE,
                new ConditionalEffect(
                        new SourceCounterThreshold(1, CounterType.MINUS_ONE_MINUS_ONE),
                        new RemoveCounterFromSourceEffect(CounterType.MINUS_ONE_MINUS_ONE, 1)));
    }
}
