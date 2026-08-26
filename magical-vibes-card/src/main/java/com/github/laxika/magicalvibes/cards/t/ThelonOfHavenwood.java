package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "227")
public class ThelonOfHavenwood extends Card {

    public ThelonOfHavenwood() {
        var fungus = new PermanentHasSubtypePredicate(CardSubtype.FUNGUS);
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                1, 1, GrantScope.ALL_CREATURES_INCLUDING_SELF, fungus, CounterType.FUNGUS, true));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}{G}",
                List.of(
                        new ExileCardFromGraveyardCost(CardSubtype.FUNGUS, true),
                        new PutCounterOnEachMatchingPermanentEffect(
                                CounterType.FUNGUS, 1, fungus, EachPermanentScope.ALL_PLAYERS)
                ),
                "{B}{G}, Exile a Fungus card from a graveyard: Put a spore counter on each Fungus on the battlefield."
        ));
    }
}
