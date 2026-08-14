package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;

import java.util.List;

@CardRegistration(set = "5DN", collectorNumber = "124")
public class GemstoneArray extends Card {

    public GemstoneArray() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new PutCountersOnSelfEffect(CounterType.CHARGE)),
                "{2}: Put a charge counter on this artifact."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.CHARGE),
                        new AwardAnyColorManaEffect()
                ),
                "Remove a charge counter from this artifact: Add one mana of any color."
        ));
    }
}
