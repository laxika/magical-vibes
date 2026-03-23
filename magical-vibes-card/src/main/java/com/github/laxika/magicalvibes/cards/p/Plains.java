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
@CardRegistration(set = "M10", collectorNumber = "230")
@CardRegistration(set = "M10", collectorNumber = "231")
@CardRegistration(set = "M10", collectorNumber = "232")
@CardRegistration(set = "M10", collectorNumber = "233")
@CardRegistration(set = "SOM", collectorNumber = "230")
@CardRegistration(set = "SOM", collectorNumber = "231")
@CardRegistration(set = "SOM", collectorNumber = "232")
@CardRegistration(set = "SOM", collectorNumber = "233")
@CardRegistration(set = "MBS", collectorNumber = "146")
@CardRegistration(set = "MBS", collectorNumber = "147")
@CardRegistration(set = "NPH", collectorNumber = "166")
@CardRegistration(set = "NPH", collectorNumber = "167")
@CardRegistration(set = "M11", collectorNumber = "230")
@CardRegistration(set = "M11", collectorNumber = "231")
@CardRegistration(set = "M11", collectorNumber = "232")
@CardRegistration(set = "M11", collectorNumber = "233")
@CardRegistration(set = "ISD", collectorNumber = "250")
@CardRegistration(set = "ISD", collectorNumber = "251")
@CardRegistration(set = "ISD", collectorNumber = "252")
@CardRegistration(set = "DOM", collectorNumber = "250")
@CardRegistration(set = "DOM", collectorNumber = "251")
@CardRegistration(set = "DOM", collectorNumber = "252")
@CardRegistration(set = "DOM", collectorNumber = "253")
@CardRegistration(set = "XLN", collectorNumber = "260")
@CardRegistration(set = "XLN", collectorNumber = "261")
@CardRegistration(set = "XLN", collectorNumber = "262")
@CardRegistration(set = "XLN", collectorNumber = "263")
public class Plains extends Card {

    public Plains() {
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.WHITE));
    }
}
