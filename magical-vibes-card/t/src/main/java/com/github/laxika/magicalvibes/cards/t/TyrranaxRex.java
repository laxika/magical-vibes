package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.ToxicEffect;

@CardRegistration(set = "ONE", collectorNumber = "189")
public class TyrranaxRex extends Card {

    public TyrranaxRex() {
        addStaticEffect(new CantBeCounteredEffect());
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL, new CounterUnlessPaysEffect(4));
        addStaticEffect(new ToxicEffect(4));
    }
}
