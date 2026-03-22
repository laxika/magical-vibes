package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoubleControllerDamageEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "XLN", collectorNumber = "132")
public class AngrathsMarauders extends Card {

    public AngrathsMarauders() {
        addEffect(EffectSlot.STATIC, new DoubleControllerDamageEffect());
    }
}
