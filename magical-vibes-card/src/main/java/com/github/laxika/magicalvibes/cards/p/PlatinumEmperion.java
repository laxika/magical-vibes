package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LifeTotalCantChangeEffect;

@CardRegistration(set = "SOM", collectorNumber = "193")
public class PlatinumEmperion extends Card {

    public PlatinumEmperion() {
        addEffect(EffectSlot.STATIC, new LifeTotalCantChangeEffect());
    }
}
