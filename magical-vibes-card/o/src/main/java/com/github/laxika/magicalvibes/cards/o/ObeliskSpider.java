package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOU", collectorNumber = "141")
@CardRegistration(set = "AKR", collectorNumber = "249")
public class ObeliskSpider extends Card {

    public ObeliskSpider() {
        // Whenever this creature deals combat damage to a creature, put a -1/-1 counter on that creature.
        // The damaged creature is baked as targetId by Triggers.dealsCombatDamageToCreature() (non-targeting).
        addTrigger(Triggers.dealsCombatDamageToCreature(),
                new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 1));

        // Whenever you put one or more -1/-1 counters on a creature, each opponent loses 1 life and you
        // gain 1 life. Once-per-creature cadence (fires once regardless of how many counters were
        // placed at once), controller-restricted. Both halves share one stack entry.
        addTrigger(Triggers.youPutMinusOneCountersOnCreature(),
                new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT),
                new GainLifeEffect(1));
    }
}
