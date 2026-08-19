package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "AER", collectorNumber = "180")
public class VerdantAutomaton extends Card {

    public VerdantAutomaton() {
        addActivatedAbility(new ActivatedAbility(false, "{3}{G}",
                List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                "{3}{G}: Put a +1/+1 counter on this creature."));
    }
}
