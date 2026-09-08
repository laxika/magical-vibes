package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

@CardRegistration(set = "SOI", collectorNumber = "230")
public class SoulSwallower extends Card {

    public SoulSwallower() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new Delirium(),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 3)));
    }
}
