package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "235")
public class PyramidOfThePantheon extends Card {

    public PyramidOfThePantheon() {
        // {2}, {T}: Add one mana of any color. Put a brick counter on this artifact.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new AwardAnyColorManaEffect(), new PutCountersOnSelfEffect(CounterType.BRICK)),
                "{2}, {T}: Add one mana of any color. Put a brick counter on this artifact."
        ));

        // {T}: Add three mana of any one color. Activate only if there are three or more brick counters on this artifact.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(3)),
                "{T}: Add three mana of any one color. Activate only if there are three or more brick counters on this artifact."
        ).withRequiredSourceCounters(CounterType.BRICK, 3));
    }
}
