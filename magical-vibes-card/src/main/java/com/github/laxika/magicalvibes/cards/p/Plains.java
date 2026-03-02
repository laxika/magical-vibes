package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "364")
@CardRegistration(set = "10E", collectorNumber = "365")
@CardRegistration(set = "10E", collectorNumber = "366")
@CardRegistration(set = "10E", collectorNumber = "367")
@CardRegistration(set = "SOM", collectorNumber = "230")
@CardRegistration(set = "SOM", collectorNumber = "231")
@CardRegistration(set = "SOM", collectorNumber = "232")
@CardRegistration(set = "SOM", collectorNumber = "233")
@CardRegistration(set = "MBS", collectorNumber = "146")
@CardRegistration(set = "MBS", collectorNumber = "147")
public class Plains extends Card {

    public Plains() {
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.WHITE));
    }
}
