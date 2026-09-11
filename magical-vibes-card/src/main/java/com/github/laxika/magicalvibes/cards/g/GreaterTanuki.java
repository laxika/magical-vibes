package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "189")
public class GreaterTanuki extends Card {

    public GreaterTanuki() {
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(new SearchLibraryEffect(
                        CardPredicateUtils.basicLand(), LibrarySearchDestination.BATTLEFIELD_TAPPED)),
                "Channel — {2}{G}, Discard this card: Search your library for a basic land card, put it onto the battlefield tapped, then shuffle."
        ));
    }
}
