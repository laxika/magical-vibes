package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageAndAddMinusCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;

@CardRegistration(set = "VIS", collectorNumber = "112")
public class Lichenthrope extends Card {

    public Lichenthrope() {
        // If damage would be dealt to this creature, put that many -1/-1 counters on it instead.
        addEffect(EffectSlot.STATIC, new PreventDamageAndAddMinusCountersEffect());

        // At the beginning of your upkeep, remove a -1/-1 counter from this creature.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new RemoveCounterFromSourceEffect(CounterType.MINUS_ONE_MINUS_ONE, 1));
    }
}
