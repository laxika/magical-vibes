package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "ICE", collectorNumber = "383")
@CardRegistration(set = "CSP", collectorNumber = "155")
@CardRegistration(set = "KHM", collectorNumber = "284")
@CardRegistration(set = "KHM", collectorNumber = "285")
public class SnowCoveredForest extends Card {

    public SnowCoveredForest() {
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.GREEN));
    }
}
