package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DevourEffect;

@CardRegistration(set = "MH1", collectorNumber = "90")
public class FeasterOfFools extends Card {

    public FeasterOfFools() {
        // Flying and convoke are auto-loaded keywords and handled by the engine.

        // Devour 2 (As this creature enters, you may sacrifice any number of creatures.
        // It enters with twice that many +1/+1 counters on it.)
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DevourEffect(2));
    }
}
