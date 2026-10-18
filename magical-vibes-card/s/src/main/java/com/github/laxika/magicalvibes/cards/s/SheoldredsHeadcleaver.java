package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ToxicEffect;

@CardRegistration(set = "ONE", collectorNumber = "109")
public class SheoldredsHeadcleaver extends Card {

    public SheoldredsHeadcleaver() {
        addEffect(EffectSlot.STATIC, new ToxicEffect(2));
    }
}
