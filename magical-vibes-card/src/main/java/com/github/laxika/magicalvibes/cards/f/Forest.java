package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "380")
@CardRegistration(set = "10E", collectorNumber = "381")
@CardRegistration(set = "10E", collectorNumber = "382")
@CardRegistration(set = "10E", collectorNumber = "383")
@CardRegistration(set = "M10", collectorNumber = "246")
@CardRegistration(set = "M10", collectorNumber = "247")
@CardRegistration(set = "M10", collectorNumber = "248")
@CardRegistration(set = "M10", collectorNumber = "249")
@CardRegistration(set = "SOM", collectorNumber = "246")
@CardRegistration(set = "SOM", collectorNumber = "247")
@CardRegistration(set = "SOM", collectorNumber = "248")
@CardRegistration(set = "SOM", collectorNumber = "249")
@CardRegistration(set = "MBS", collectorNumber = "154")
@CardRegistration(set = "MBS", collectorNumber = "155")
@CardRegistration(set = "NPH", collectorNumber = "174")
@CardRegistration(set = "NPH", collectorNumber = "175")
@CardRegistration(set = "M11", collectorNumber = "246")
@CardRegistration(set = "M11", collectorNumber = "247")
@CardRegistration(set = "M11", collectorNumber = "248")
@CardRegistration(set = "M11", collectorNumber = "249")
@CardRegistration(set = "ISD", collectorNumber = "262")
@CardRegistration(set = "ISD", collectorNumber = "263")
@CardRegistration(set = "ISD", collectorNumber = "264")
@CardRegistration(set = "DOM", collectorNumber = "266")
@CardRegistration(set = "DOM", collectorNumber = "267")
@CardRegistration(set = "DOM", collectorNumber = "268")
@CardRegistration(set = "DOM", collectorNumber = "269")
public class Forest extends Card {

    public Forest() {
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.GREEN));
    }
}
