package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "246")
public class ShadowedCaravel extends Card {

    public ShadowedCaravel() {
        // Whenever a creature you control explores, put a +1/+1 counter on Shadowed Caravel.
        addTrigger(Triggers.creatureYouControlExplores(), new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));

        // Crew 2
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(2), AnimatePermanentsEffect.crew()),
                "Crew 2"
        ));
    }
}
