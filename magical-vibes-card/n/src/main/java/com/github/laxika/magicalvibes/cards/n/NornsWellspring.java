package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

import java.util.List;

@CardRegistration(set = "ONE", collectorNumber = "24")
public class NornsWellspring extends Card {

    public NornsWellspring() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new ScryEffect(1));
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new PutCountersOnSelfEffect(CounterType.OIL));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new RemoveCounterFromSourceCost(2, CounterType.OIL),
                        new DrawCardEffect()
                ),
                "{1}, {T}, Remove two oil counters from this artifact: Draw a card."
        ));
    }
}
