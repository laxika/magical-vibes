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
@CardRegistration(set = "ME2", collectorNumber = "245")
@CardRegistration(set = "SLD", collectorNumber = "5")
@CardRegistration(set = "SLD", collectorNumber = "329")
@CardRegistration(set = "SLD", collectorNumber = "1477")
@CardRegistration(set = "MH1", collectorNumber = "254")
public class SnowCoveredForest extends Card {

    public SnowCoveredForest() {
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.GREEN));
    }
}
