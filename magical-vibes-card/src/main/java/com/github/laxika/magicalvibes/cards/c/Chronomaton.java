package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "M13", collectorNumber = "201")
public class Chronomaton extends Card {

    public Chronomaton() {
        // {1}, {T}: Put a +1/+1 counter on this creature.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                "{1}, {T}: Put a +1/+1 counter on this creature."
        ));
    }
}
