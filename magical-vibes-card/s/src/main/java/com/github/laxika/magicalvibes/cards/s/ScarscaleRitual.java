package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnControlledCreatureCost;

@CardRegistration(set = "SHM", collectorNumber = "175")
public class ScarscaleRitual extends Card {

    public ScarscaleRitual() {
        // As an additional cost to cast this spell, put a -1/-1 counter on a creature you control.
        addEffect(EffectSlot.SPELL, new PutCounterOnControlledCreatureCost(CounterType.MINUS_ONE_MINUS_ONE, 1));
        // Draw two cards.
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
    }
}
