package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveXCountersFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "132")
public class NightDealings extends Card {

    public NightDealings() {
        // Whenever a source you control deals damage to another player, put that many theft
        // counters on this enchantment.
        addEffect(EffectSlot.ON_ALLY_SOURCE_DEALS_DAMAGE_TO_OPPONENT,
                new PutCountersOnSelfEffect(CounterType.THEFT, new EventValue()));

        // {2}{B}{B}, Remove X theft counters from this enchantment: Search your library for a
        // nonland card with mana value X, reveal it, put it into your hand, then shuffle.
        // The search reveals the chosen card on its own for a filtered HAND tutor.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}{B}",
                List.of(
                        new RemoveXCountersFromSourceCost(CounterType.THEFT),
                        new SearchLibraryEffect(
                                new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                                LibrarySearchDestination.HAND,
                                new ManaValueBound(true, 0))
                ),
                "{2}{B}{B}, Remove X theft counters from Night Dealings: Search your library for a "
                        + "nonland card with mana value X, reveal it, put it into your hand, then shuffle."
        ));
    }
}
