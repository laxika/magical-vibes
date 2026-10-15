package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DevourEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;

@CardRegistration(set = "ALA", collectorNumber = "95")
public class CalderaHellion extends Card {

    public CalderaHellion() {
        // Devour 1 (As this creature enters, you may sacrifice any number of creatures.
        // It enters with that many +1/+1 counters on it.)
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DevourEffect(1));

        // When this creature enters, it deals 3 damage to each creature.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MassDamageEffect(3));
    }
}
