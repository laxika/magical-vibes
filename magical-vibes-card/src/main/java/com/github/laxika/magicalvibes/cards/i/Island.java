package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "368")
@CardRegistration(set = "10E", collectorNumber = "369")
@CardRegistration(set = "10E", collectorNumber = "370")
@CardRegistration(set = "10E", collectorNumber = "371")
@CardRegistration(set = "M10", collectorNumber = "234")
@CardRegistration(set = "M10", collectorNumber = "235")
@CardRegistration(set = "M10", collectorNumber = "236")
@CardRegistration(set = "M10", collectorNumber = "237")
@CardRegistration(set = "SOM", collectorNumber = "234")
@CardRegistration(set = "SOM", collectorNumber = "235")
@CardRegistration(set = "SOM", collectorNumber = "236")
@CardRegistration(set = "SOM", collectorNumber = "237")
@CardRegistration(set = "MBS", collectorNumber = "148")
@CardRegistration(set = "MBS", collectorNumber = "149")
@CardRegistration(set = "NPH", collectorNumber = "168")
@CardRegistration(set = "NPH", collectorNumber = "169")
@CardRegistration(set = "M11", collectorNumber = "234")
@CardRegistration(set = "M11", collectorNumber = "235")
@CardRegistration(set = "M11", collectorNumber = "236")
@CardRegistration(set = "M11", collectorNumber = "237")
@CardRegistration(set = "ISD", collectorNumber = "253")
@CardRegistration(set = "ISD", collectorNumber = "254")
@CardRegistration(set = "ISD", collectorNumber = "255")
@CardRegistration(set = "DOM", collectorNumber = "254")
@CardRegistration(set = "DOM", collectorNumber = "255")
@CardRegistration(set = "DOM", collectorNumber = "256")
@CardRegistration(set = "DOM", collectorNumber = "257")
@CardRegistration(set = "XLN", collectorNumber = "264")
@CardRegistration(set = "XLN", collectorNumber = "265")
@CardRegistration(set = "XLN", collectorNumber = "266")
@CardRegistration(set = "XLN", collectorNumber = "267")
public class Island extends Card {

    public Island() {
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.BLUE));
    }
}
