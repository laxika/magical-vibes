package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;

@CardRegistration(set = "XLN", collectorNumber = "179")
public class CarnageTyrant extends Card {

    public CarnageTyrant() {
        addEffect(EffectSlot.STATIC, new CantBeCounteredEffect());
    }
}
