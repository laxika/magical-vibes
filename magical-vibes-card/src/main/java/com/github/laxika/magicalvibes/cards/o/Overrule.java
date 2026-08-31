package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "DIS", collectorNumber = "120")
public class Overrule extends Card {

    public Overrule() {
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(0, true, false));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(new XValue()));
    }
}
