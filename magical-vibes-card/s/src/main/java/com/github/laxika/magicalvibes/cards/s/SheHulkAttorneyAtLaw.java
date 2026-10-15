package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DoublePlusOneCountersOnControlledCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "745")
public class SheHulkAttorneyAtLaw extends Card {

    public SheHulkAttorneyAtLaw() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{6}{G/W}",
                List.of(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        new DoublePlusOneCountersOnControlledCreaturesEffect()
                ),
                "Power-up — {6}{G/W}: Put a +1/+1 counter on She-Hulk. Then double the number of +1/+1 counters on each creature you control. "
                        + "(Activate each power-up ability only once. Reduce the cost by her mana cost if she entered this turn.)"
        ).withPowerUp());
    }
}
