package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "232")
public class LuxaRiverShrine extends Card {

    public LuxaRiverShrine() {
        // {1}, {T}: You gain 1 life. Put a brick counter on this artifact.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new GainLifeEffect(1),
                        new PutCountersOnSelfEffect(CounterType.BRICK)
                ),
                "{1}, {T}: You gain 1 life. Put a brick counter on Luxa River Shrine."
        ));

        // {T}: You gain 2 life. Activate only if there are three or more brick counters on this artifact.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new GainLifeEffect(2)),
                "{T}: You gain 2 life. Activate only if there are three or more brick counters on Luxa River Shrine."
        ).withRequiredSourceCounters(CounterType.BRICK, 3));
    }
}
