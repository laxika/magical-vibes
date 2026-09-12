package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "VMA", collectorNumber = "226")
public class RealmSeekers extends Card {

    public RealmSeekers() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE,
                        new CardsInHand(CountScope.ANY_PLAYER)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.PLUS_ONE_PLUS_ONE),
                        new SearchLibraryEffect(new CardTypePredicate(CardType.LAND))
                ),
                "{2}{G}, Remove a +1/+1 counter from this creature: Search your library for a land card, reveal it, put it into your hand, then shuffle."
        ));
    }
}
