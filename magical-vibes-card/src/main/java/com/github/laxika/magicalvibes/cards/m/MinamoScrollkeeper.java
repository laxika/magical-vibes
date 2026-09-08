package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.IncreaseControllerMaxHandSizeEffect;

@CardRegistration(set = "SOK", collectorNumber = "45")
public class MinamoScrollkeeper extends Card {

    public MinamoScrollkeeper() {
        addEffect(EffectSlot.STATIC, new IncreaseControllerMaxHandSizeEffect(1));
    }
}
