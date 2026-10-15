package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.EarthbendTargetLandEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "191")
public class RebelliousCaptives extends Card {

    public RebelliousCaptives() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{6}",
                List.of(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2),
                        new EarthbendTargetLandEffect(2)
                ),
                "Exhaust — {6}: Put two +1/+1 counters on this creature, then earthbend 2. "
                        + "(Activate each exhaust ability only once.)"
        ).withMaxActivationsPerGame(1).withExhaust());
    }
}
