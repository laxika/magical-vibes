package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

@CardRegistration(set = "FDN", collectorNumber = "11")
public class ExemplarOfLight extends Card {

    public ExemplarOfLight() {
        // Whenever you gain life, put a +1/+1 counter on this creature.
        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));

        // Whenever you put one or more +1/+1 counters on this creature, draw a card.
        addEffect(EffectSlot.ON_SELF_PLUS_ONE_PLUS_ONE_COUNTERS_PUT,
                new OncePerTurnTriggerEffect(new DrawCardEffect()));
    }
}
