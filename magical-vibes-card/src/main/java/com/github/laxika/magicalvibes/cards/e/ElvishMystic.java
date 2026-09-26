package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

@CardRegistration(set = "M14", collectorNumber = "169")
@CardRegistration(set = "C14", collectorNumber = "191")
@CardRegistration(set = "M15", collectorNumber = "173")
@CardRegistration(set = "DDU", collectorNumber = "7")
@CardRegistration(set = "SLD", collectorNumber = "475")
@CardRegistration(set = "SLD", collectorNumber = "805")
@CardRegistration(set = "EA1", collectorNumber = "13")
@CardRegistration(set = "TSR", collectorNumber = "360")
@CardRegistration(set = "SOC", collectorNumber = "266")
@CardRegistration(set = "CMM", collectorNumber = "284")
@CardRegistration(set = "CMM", collectorNumber = "648")
@CardRegistration(set = "LTC", collectorNumber = "238")
public class ElvishMystic extends Card {

    public ElvishMystic() {
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.GREEN));
    }
}
