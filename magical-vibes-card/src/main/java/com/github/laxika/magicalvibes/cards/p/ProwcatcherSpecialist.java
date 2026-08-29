package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "142")
public class ProwcatcherSpecialist extends Card {

    public ProwcatcherSpecialist() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{R}",
                List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2)),
                "Exhaust — {3}{R}: Put two +1/+1 counters on this creature."
                        + " (Activate each exhaust ability only once.)"
        ).withMaxActivationsPerGame(1).withExhaust());
    }
}
