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
@CardRegistration(set = "ME2", collectorNumber = "242")
@CardRegistration(set = "SLD", collectorNumber = "2")
@CardRegistration(set = "SLD", collectorNumber = "326")
@CardRegistration(set = "SLD", collectorNumber = "1474")
@CardRegistration(set = "MH1", collectorNumber = "251")
public class SnowCoveredIsland extends Card {

    public SnowCoveredIsland() {
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.BLUE));
    }
}
