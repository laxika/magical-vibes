package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "ICE", collectorNumber = "371")
@CardRegistration(set = "CSP", collectorNumber = "152")
@CardRegistration(set = "KHM", collectorNumber = "278")
@CardRegistration(set = "KHM", collectorNumber = "279")
public class SnowCoveredIsland extends Card {

    public SnowCoveredIsland() {
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.BLUE));
    }
}
