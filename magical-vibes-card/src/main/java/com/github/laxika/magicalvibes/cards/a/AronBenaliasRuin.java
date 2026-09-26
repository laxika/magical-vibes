package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "193")
public class AronBenaliasRuin extends Card {

    public AronBenaliasRuin() {
        // Menace (keyword from Scryfall)

        // {W}{B}, {T}, Sacrifice another creature: Put a +1/+1 counter on each creature you control.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}{B}",
                List.of(
                        new SacrificeCreatureCost(false, false, false, true),
                        new PutCounterOnEachControlledPermanentEffect(
                                CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate())
                ),
                "{W}{B}, {T}, Sacrifice another creature: Put a +1/+1 counter on each creature you control."
        ));
    }
}
