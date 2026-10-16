package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ToxicEffect;

@CardRegistration(set = "ONE", collectorNumber = "170")
public class IchorspitBasilisk extends Card {

    public IchorspitBasilisk() {
        addEffect(EffectSlot.STATIC, new ToxicEffect(1));
    }
}
