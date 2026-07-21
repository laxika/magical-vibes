package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;

@CardRegistration(set = "HOU", collectorNumber = "141")
public class ObeliskSpider extends Card {

    public ObeliskSpider() {
        // Whenever this creature deals combat damage to a creature, put a -1/-1 counter on that creature.
        // The damaged creature is baked as targetId by ON_COMBAT_DAMAGE_TO_CREATURE (non-targeting).
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_CREATURE,
                new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 1));

        // Whenever you put one or more -1/-1 counters on a creature, each opponent loses 1 life and you
        // gain 1 life. Once-per-creature cadence (fires once regardless of how many counters were
        // placed at once), controller-restricted. Both halves share one stack entry.
        addEffect(EffectSlot.ON_YOU_PUT_MINUS_ONE_MINUS_ONE_COUNTERS_ON_CREATURE,
                new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.ON_YOU_PUT_MINUS_ONE_MINUS_ONE_COUNTERS_ON_CREATURE,
                new GainLifeEffect(1));
    }
}
