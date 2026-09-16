package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.ControlledPermanentsEnterWithAdditionalCountersByAmountEffect;
import com.github.laxika.magicalvibes.model.effect.DevourEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "2XM", collectorNumber = "155")
public class BloodsporeThrinax extends Card {

    public BloodsporeThrinax() {
        // Devour 1 (As this creature enters, you may sacrifice any number of creatures.
        // It enters with that many +1/+1 counters on it.)
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DevourEffect(1));

        // Each other creature you control enters with an additional X +1/+1 counters on it,
        // where X is the number of +1/+1 counters on this creature.
        addEffect(EffectSlot.STATIC, new ControlledPermanentsEnterWithAdditionalCountersByAmountEffect(
                new PermanentIsCreaturePredicate(),
                new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE)));
    }
}
