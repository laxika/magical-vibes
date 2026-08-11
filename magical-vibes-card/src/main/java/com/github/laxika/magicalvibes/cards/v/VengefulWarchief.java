package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "M20", collectorNumber = "121")
public class VengefulWarchief extends Card {

    public VengefulWarchief() {
        // Whenever you lose life for the first time each turn, put a +1/+1 counter on this creature.
        addEffect(EffectSlot.ON_CONTROLLER_LOSES_LIFE,
                new OncePerTurnTriggerEffect(new PutCountersOnSourceEffect(1, 1, 1)));
    }
}
