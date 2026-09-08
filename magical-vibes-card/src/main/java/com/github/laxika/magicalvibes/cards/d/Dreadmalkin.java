package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "87")
public class Dreadmalkin extends Card {

    public Dreadmalkin() {
        // {2}{B}, Sacrifice another creature or planeswalker: Put two +1/+1 counters on this creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentIsPlaneswalkerPredicate()
                                )),
                                "Sacrifice another creature or planeswalker"
                        ),
                        new PutCountersOnSourceEffect(1, 1, 2)
                ),
                "{2}{B}, Sacrifice another creature or planeswalker: Put two +1/+1 counters on this creature."
        ));
    }
}
