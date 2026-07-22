package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "118")
public class IndulgentAristocrat extends Card {

    public IndulgentAristocrat() {
        // Lifelink (keyword from Scryfall)

        // {2}, Sacrifice a creature: Put a +1/+1 counter on each Vampire you control.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new SacrificeCreatureCost(),
                        new PutCounterOnEachControlledPermanentEffect(
                                CounterType.PLUS_ONE_PLUS_ONE, 1,
                                new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE)
                        )
                ),
                "{2}, Sacrifice a creature: Put a +1/+1 counter on each Vampire you control."
        ));
    }
}
