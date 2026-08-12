package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

@CardRegistration(set = "M20", collectorNumber = "344")
public class WoodlandMystic extends Card {

    public WoodlandMystic() {
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.GREEN));
    }
}
