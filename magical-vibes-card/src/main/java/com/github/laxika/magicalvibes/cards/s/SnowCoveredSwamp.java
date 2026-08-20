package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "ICE", collectorNumber = "372")
@CardRegistration(set = "CSP", collectorNumber = "153")
@CardRegistration(set = "KHM", collectorNumber = "280")
@CardRegistration(set = "KHM", collectorNumber = "281")
public class SnowCoveredSwamp extends Card {

    public SnowCoveredSwamp() {
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.BLACK));
    }
}
