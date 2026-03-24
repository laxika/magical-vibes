package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfAsCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnSelfEffect;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "246")
public class ShadowedCaravel extends Card {

    public ShadowedCaravel() {
        // Whenever a creature you control explores, put a +1/+1 counter on Shadowed Caravel.
        addEffect(EffectSlot.ON_ALLY_CREATURE_EXPLORES, new PutCounterOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));

        // Crew 2
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(2), new AnimateSelfAsCreatureEffect()),
                "Crew 2"
        ));
    }
}
