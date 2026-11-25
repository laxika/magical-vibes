package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "M21", collectorNumber = "152")
public class Hobblefiend extends Card {

    public Hobblefiend() {
        // Trample (keyword from Scryfall)

        // {1}, Sacrifice another creature: Put a +1/+1 counter on this creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new SacrificeCreatureCost(false, false, false, true),
                        PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE)
                ),
                "{1}, Sacrifice another creature: Put a +1/+1 counter on Hobblefiend."
        ));
    }
}
