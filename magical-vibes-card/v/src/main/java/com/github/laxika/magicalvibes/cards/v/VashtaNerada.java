package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "73")
@CardRegistration(set = "WHO", collectorNumber = "376")
@CardRegistration(set = "WHO", collectorNumber = "678")
@CardRegistration(set = "WHO", collectorNumber = "967")
public class VashtaNerada extends Card {

    public VashtaNerada() {
        // Morbid — At the beginning of each end step, if a creature died this turn,
        // put a +1/+1 counter on this creature.
        addTrigger(Triggers.endStep(ActivePlayer.ANY), new ConditionalEffect(
                new Morbid(),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)));
    }
}
