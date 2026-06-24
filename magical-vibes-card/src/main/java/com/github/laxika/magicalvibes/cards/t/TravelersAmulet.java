package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardsToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "234")
public class TravelersAmulet extends Card {

    public TravelersAmulet() {
        // {1}, Sacrifice Traveler's Amulet: Search your library for a basic land card, reveal it, put it into your hand, then shuffle.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new SacrificeSelfCost(), new SearchLibraryForCardsToHandEffect(CardPredicateUtils.basicLand())),
                "{1}, Sacrifice Traveler's Amulet: Search your library for a basic land card, reveal it, put it into your hand, then shuffle."
        ));
    }
}
