package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.MayCastInstantOrSorceryFromHandWithManaValueEqualToSourceCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveXCountersFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

import java.util.List;

@CardRegistration(set = "SOI", collectorNumber = "252")
public class BrainInAJar extends Card {

    public BrainInAJar() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new PutCountersOnSelfEffect(CounterType.CHARGE),
                        new MayCastInstantOrSorceryFromHandWithManaValueEqualToSourceCountersEffect(
                                CounterType.CHARGE)
                ),
                "{1}, {T}: Put a charge counter on this artifact, then you may cast an instant or sorcery spell "
                        + "with mana value equal to the number of charge counters on this artifact from your hand "
                        + "without paying its mana cost."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(
                        new RemoveXCountersFromSourceCost(CounterType.CHARGE),
                        new ScryEffect(new XValue())
                ),
                "{3}, {T}, Remove X charge counters from this artifact: Scry X."
        ));
    }
}
