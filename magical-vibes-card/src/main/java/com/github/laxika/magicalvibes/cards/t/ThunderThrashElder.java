package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DevourEffect;

@CardRegistration(set = "ALA", collectorNumber = "117")
public class ThunderThrashElder extends Card {

    public ThunderThrashElder() {
        // Devour 3 (As this creature enters, you may sacrifice any number of creatures.
        // It enters with three times that many +1/+1 counters on it.)
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DevourEffect(3));
    }
}
