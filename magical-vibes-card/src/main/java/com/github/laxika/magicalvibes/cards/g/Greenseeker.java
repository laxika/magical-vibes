package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "198")
public class Greenseeker extends Card {

    public Greenseeker() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new SearchLibraryEffect(CardPredicateUtils.basicLand())
                ),
                "{G}, {T}, Discard a card: Search your library for a basic land card, reveal it, put it into your hand, then shuffle."
        ));
    }
}
