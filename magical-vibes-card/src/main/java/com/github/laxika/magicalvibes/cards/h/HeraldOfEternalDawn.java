package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantLoseGameEffect;

@CardRegistration(set = "FDN", collectorNumber = "17")
public class HeraldOfEternalDawn extends Card {

    public HeraldOfEternalDawn() {
        addEffect(EffectSlot.STATIC, new CantLoseGameEffect());
    }
}
