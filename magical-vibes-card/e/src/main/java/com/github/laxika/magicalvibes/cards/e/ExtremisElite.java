package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "763")
public class ExtremisElite extends Card {

    public ExtremisElite() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{R}",
                List.of(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2),
                        new DealDamageToAnyTargetEffect(1)
                ),
                "Power-up — {4}{R}: Put two +1/+1 counters on Extremis Elite. It deals 1 damage to any target. "
                        + "Activate each power-up ability only once. Reduce the cost by its mana cost if it entered this turn."
        ).withPowerUp());
    }
}
