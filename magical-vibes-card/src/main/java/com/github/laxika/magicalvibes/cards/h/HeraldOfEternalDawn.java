package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantLoseGameEffect;

@CardRegistration(set = "FDN", collectorNumber = "17")
@CardRegistration(set = "FDN", collectorNumber = "299")
@CardRegistration(set = "FDN", collectorNumber = "368")
@CardRegistration(set = "FDN", collectorNumber = "423")
@CardRegistration(set = "FDN", collectorNumber = "433")
@CardRegistration(set = "FDN", collectorNumber = "446")
public class HeraldOfEternalDawn extends Card {

    public HeraldOfEternalDawn() {
        addEffect(EffectSlot.STATIC, new CantLoseGameEffect());
    }
}
