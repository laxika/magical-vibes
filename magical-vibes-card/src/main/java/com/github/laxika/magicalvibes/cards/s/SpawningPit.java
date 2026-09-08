package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DST", collectorNumber = "141")
public class SpawningPit extends Card {

    public SpawningPit() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeCreatureCost(),
                        new PutCountersOnSelfEffect(CounterType.CHARGE)
                ),
                "Sacrifice a creature: Put a charge counter on this artifact."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new RemoveCounterFromSourceCost(2, CounterType.CHARGE),
                        new CreateTokenEffect("Spawn", 2, 2, null,
                                List.of(CardSubtype.SPAWN), Set.of(), Set.of(CardType.ARTIFACT))
                ),
                "{1}, Remove two charge counters from this artifact: Create a 2/2 colorless Spawn artifact creature token."
        ));
    }
}
