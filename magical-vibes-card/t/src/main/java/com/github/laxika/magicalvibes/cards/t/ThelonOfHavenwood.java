package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "227")
@CardRegistration(set = "TSR", collectorNumber = "238")
public class ThelonOfHavenwood extends Card {

    public ThelonOfHavenwood() {
        var fungus = new PermanentHasSubtypePredicate(CardSubtype.FUNGUS);
        addStaticEffect(new StaticBoostEffect(
                1, 1, Subjects.allCreatures(), fungus, CounterType.SPORE, true));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}{G}",
                List.of(
                        new ExileCardFromGraveyardCost(CardSubtype.FUNGUS, true),
                        PutCountersEffect.onEach(CounterType.SPORE, 1, fungus)
                ),
                "{B}{G}, Exile a Fungus card from a graveyard: Put a spore counter on each Fungus on the battlefield."
        ));
    }
}
