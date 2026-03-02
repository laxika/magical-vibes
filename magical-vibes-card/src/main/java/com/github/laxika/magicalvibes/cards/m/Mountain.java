package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "376")
@CardRegistration(set = "10E", collectorNumber = "377")
@CardRegistration(set = "10E", collectorNumber = "378")
@CardRegistration(set = "10E", collectorNumber = "379")
@CardRegistration(set = "SOM", collectorNumber = "242")
@CardRegistration(set = "SOM", collectorNumber = "243")
@CardRegistration(set = "SOM", collectorNumber = "244")
@CardRegistration(set = "SOM", collectorNumber = "245")
@CardRegistration(set = "MBS", collectorNumber = "152")
@CardRegistration(set = "MBS", collectorNumber = "153")
public class Mountain extends Card {

    public Mountain() {
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.RED));
    }
}
