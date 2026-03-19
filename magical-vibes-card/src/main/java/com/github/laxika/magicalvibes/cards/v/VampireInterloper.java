package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;

@CardRegistration(set = "ISD", collectorNumber = "123")
public class VampireInterloper extends Card {

    public VampireInterloper() {
        addEffect(EffectSlot.STATIC, new CantBlockEffect());
    }
}
