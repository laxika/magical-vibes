package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;

@CardRegistration(set = "HOB", collectorNumber = "126")
public class GiganticBigBear extends Card {

    public GiganticBigBear() {
        addEffect(EffectSlot.STATIC, new CantBeCounteredEffect());
    }
}
