package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.PutTypedCounterOnSourceCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "M15", collectorNumber = "209")
public class YisanTheWandererBard extends Card {

    public YisanTheWandererBard() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{G}",
                List.of(
                        new PutTypedCounterOnSourceCost(CounterType.VERSE),
                        // The verse counter is paid as a cost, so it already counts toward the mana value.
                        new SearchLibraryEffect(
                                new CardTypePredicate(CardType.CREATURE),
                                LibrarySearchDestination.BATTLEFIELD,
                                new ManaValueBound(new CountersOnSource(CounterType.VERSE), true, 0))),
                "{2}{G}, {T}, Put a verse counter on this creature: Search your library for a creature card with "
                        + "mana value equal to the number of verse counters on this creature, put it onto the battlefield, then shuffle."
        ));
    }
}
