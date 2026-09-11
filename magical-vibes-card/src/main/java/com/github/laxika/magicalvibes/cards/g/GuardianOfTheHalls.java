package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "HOB", collectorNumber = "127")
public class GuardianOfTheHalls extends Card {

    public GuardianOfTheHalls() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{G}{G}",
                List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 3)),
                "{5}{G}{G}: Put three +1/+1 counters on this creature."
        ));
    }
}
