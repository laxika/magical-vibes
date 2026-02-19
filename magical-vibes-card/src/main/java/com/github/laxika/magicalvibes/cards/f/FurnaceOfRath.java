package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "204")
public class FurnaceOfRath extends Card {

    public FurnaceOfRath() {
        addEffect(EffectSlot.STATIC, new DoubleDamageEffect());
    }
}
