package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "193")
public class IntrepidTenderfoot extends Card {

    public IntrepidTenderfoot() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                "{3}: Put a +1/+1 counter on this creature. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
