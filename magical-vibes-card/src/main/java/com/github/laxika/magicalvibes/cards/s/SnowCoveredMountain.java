package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "ICE", collectorNumber = "379")
@CardRegistration(set = "CSP", collectorNumber = "154")
@CardRegistration(set = "KHM", collectorNumber = "282")
@CardRegistration(set = "KHM", collectorNumber = "283")
public class SnowCoveredMountain extends Card {

    public SnowCoveredMountain() {
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.RED));
    }
}
