package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "ICE", collectorNumber = "367")
@CardRegistration(set = "CSP", collectorNumber = "151")
@CardRegistration(set = "KHM", collectorNumber = "276")
@CardRegistration(set = "KHM", collectorNumber = "277")
public class SnowCoveredPlains extends Card {

    public SnowCoveredPlains() {
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.WHITE));
    }
}
