package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "AVR", collectorNumber = "87")
@CardRegistration(set = "UMA", collectorNumber = "86")
@CardRegistration(set = "2X2", collectorNumber = "71")
@CardRegistration(set = "SIS", collectorNumber = "26")
public class BloodflowConnoisseur extends Card {

    public BloodflowConnoisseur() {
        // Sacrifice a creature: Put a +1/+1 counter on this creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeCreatureCost(),
                        PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE)
                ),
                "Sacrifice a creature: Put a +1/+1 counter on Bloodflow Connoisseur."
        ));
    }
}
