package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DevourEffect;

@CardRegistration(set = "PC2", collectorNumber = "50")
public class PreyseizerDragon extends Card {

    public PreyseizerDragon() {
        // Devour 2 (As this creature enters, you may sacrifice any number of creatures.
        // It enters with twice that many +1/+1 counters on it.)
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DevourEffect(2));

        // Whenever this creature attacks, it deals damage to any target equal to the number
        // of +1/+1 counters on it.
        addEffect(EffectSlot.ON_ATTACK,
                new DealDamageToAnyTargetEffect(new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE)));
    }
}
