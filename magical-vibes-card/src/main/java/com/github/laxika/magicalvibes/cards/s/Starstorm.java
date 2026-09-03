package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;

@CardRegistration(set = "ONS", collectorNumber = "238")
public class Starstorm extends Card {

    public Starstorm() {
        addEffect(EffectSlot.SPELL, new MassDamageEffect(new XValue(), false));
        addCycling("{3}");
    }
}
