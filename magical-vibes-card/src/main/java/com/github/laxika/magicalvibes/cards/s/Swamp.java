package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "372")
@CardRegistration(set = "10E", collectorNumber = "373")
@CardRegistration(set = "10E", collectorNumber = "374")
@CardRegistration(set = "10E", collectorNumber = "375")
@CardRegistration(set = "SOM", collectorNumber = "238")
@CardRegistration(set = "SOM", collectorNumber = "239")
@CardRegistration(set = "SOM", collectorNumber = "240")
@CardRegistration(set = "SOM", collectorNumber = "241")
@CardRegistration(set = "MBS", collectorNumber = "150")
@CardRegistration(set = "MBS", collectorNumber = "151")
@CardRegistration(set = "NPH", collectorNumber = "170")
@CardRegistration(set = "NPH", collectorNumber = "171")
@CardRegistration(set = "M11", collectorNumber = "238")
@CardRegistration(set = "M11", collectorNumber = "239")
@CardRegistration(set = "M11", collectorNumber = "240")
@CardRegistration(set = "M11", collectorNumber = "241")
public class Swamp extends Card {

    public Swamp() {
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.BLACK));
    }
}
