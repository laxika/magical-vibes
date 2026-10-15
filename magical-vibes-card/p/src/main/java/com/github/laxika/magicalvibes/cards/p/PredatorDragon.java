package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DevourEffect;

@CardRegistration(set = "ALA", collectorNumber = "109")
public class PredatorDragon extends Card {

    public PredatorDragon() {
        // Flying, haste (auto-loaded keywords).

        // Devour 2 (As this creature enters, you may sacrifice any number of creatures.
        // It enters with twice that many +1/+1 counters on it.)
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DevourEffect(2));
    }
}
