package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

@CardRegistration(set = "ZEN", collectorNumber = "164")
public class GreenweaverDruid extends Card {

    public GreenweaverDruid() {
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.GREEN, 2));
    }
}
