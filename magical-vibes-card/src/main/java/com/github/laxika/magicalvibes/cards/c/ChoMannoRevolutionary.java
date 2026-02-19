package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "12")
public class ChoMannoRevolutionary extends Card {

    public ChoMannoRevolutionary() {
        addEffect(EffectSlot.STATIC, new PreventAllDamageEffect());
    }
}
