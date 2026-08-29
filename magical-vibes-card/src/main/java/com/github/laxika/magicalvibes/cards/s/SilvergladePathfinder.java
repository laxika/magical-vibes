package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "270")
public class SilvergladePathfinder extends Card {

    public SilvergladePathfinder() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{G}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new SearchLibraryEffect(CardPredicateUtils.basicLand(), LibrarySearchDestination.BATTLEFIELD_TAPPED)
                ),
                "{1}{G}, {T}, Discard a card: Search your library for a basic land card, put that card onto the battlefield tapped, then shuffle."
        ));
    }
}
