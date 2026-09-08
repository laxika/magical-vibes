package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;

import java.util.List;

@CardRegistration(set = "M19", collectorNumber = "238")
@CardRegistration(set = "MMQ", collectorNumber = "304")
public class MagistratesScepter extends Card {

    public MagistratesScepter() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new PutCountersOnSelfEffect(CounterType.CHARGE)),
                "{4}, {T}: Put a charge counter on this artifact."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(3, CounterType.CHARGE),
                        new ControllerExtraTurnEffect(1)
                ),
                "{T}, Remove three charge counters from this artifact: Take an extra turn after this one."
        ));
    }
}
