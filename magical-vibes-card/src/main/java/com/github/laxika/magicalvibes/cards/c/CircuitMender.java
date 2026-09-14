package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "NEO", collectorNumber = "242")
public class CircuitMender extends Card {

    public CircuitMender() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(2));
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new DrawCardEffect());
    }
}
