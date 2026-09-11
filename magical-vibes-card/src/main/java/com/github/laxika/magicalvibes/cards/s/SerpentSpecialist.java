package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "186")
public class SerpentSpecialist extends Card {

    public SerpentSpecialist() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{G}",
                List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2)),
                "Power-up — {3}{G}: Put two +1/+1 counters on this creature. Activate each power-up "
                        + "ability only once. Reduce the cost by its mana cost if it entered this turn."
        ).withPowerUp());
    }
}
