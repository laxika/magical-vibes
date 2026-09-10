package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

@CardRegistration(set = "SOM", collectorNumber = "202")
@CardRegistration(set = "MRD", collectorNumber = "241")
@CardRegistration(set = "DDF", collectorNumber = "43")
@CardRegistration(set = "HOP", collectorNumber = "126")
public class SilverMyr extends Card {

    public SilverMyr() {
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.BLUE));
    }
}
