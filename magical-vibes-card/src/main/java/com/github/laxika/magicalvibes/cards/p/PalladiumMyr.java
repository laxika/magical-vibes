package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

@CardRegistration(set = "SOM", collectorNumber = "190")
@CardRegistration(set = "M21", collectorNumber = "234")
public class PalladiumMyr extends Card {

    public PalladiumMyr() {
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.COLORLESS, 2));
    }
}
