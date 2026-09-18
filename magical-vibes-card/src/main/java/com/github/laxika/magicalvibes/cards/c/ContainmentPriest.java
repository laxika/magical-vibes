package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileUncastEnteringCreaturesEffect;

@CardRegistration(set = "M21", collectorNumber = "13")
@CardRegistration(set = "MP2", collectorNumber = "3")
@CardRegistration(set = "UMA", collectorNumber = "11")
@CardRegistration(set = "TSR", collectorNumber = "292")
@CardRegistration(set = "MB2", collectorNumber = "7")
public class ContainmentPriest extends Card {

    public ContainmentPriest() {
        addEffect(EffectSlot.STATIC, new ExileUncastEnteringCreaturesEffect(true));
    }
}
