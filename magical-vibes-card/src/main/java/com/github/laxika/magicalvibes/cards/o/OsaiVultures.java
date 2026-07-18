package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;

import java.util.List;

@CardRegistration(set = "4ED", collectorNumber = "38")
public class OsaiVultures extends Card {

    public OsaiVultures() {
        // At the beginning of each end step, if a creature died this turn, put a carrion counter on it.
        addEffect(EffectSlot.END_STEP_TRIGGERED, new ConditionalEffect(
                new Morbid(),
                new PutCountersOnSelfEffect(CounterType.CARRION)));

        // Remove two carrion counters from this creature: This creature gets +1/+1 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(2, CounterType.CARRION),
                        new BoostSelfEffect(1, 1)
                ),
                "Remove two carrion counters from Osai Vultures: Osai Vultures gets +1/+1 until end of turn."
        ));
    }
}
