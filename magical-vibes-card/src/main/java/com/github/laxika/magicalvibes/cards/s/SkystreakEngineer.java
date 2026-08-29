package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "61")
public class SkystreakEngineer extends Card {

    public SkystreakEngineer() {
        // Exhaust — {4}{U}: Put two +1/+1 counters on this creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{U}",
                List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2)),
                "Exhaust — {4}{U}: Put two +1/+1 counters on this creature."
                        + " (Activate each exhaust ability only once.)"
        ).withMaxActivationsPerGame(1).withExhaust());
    }
}
