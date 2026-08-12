package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;

@CardRegistration(set = "ZEN", collectorNumber = "161")
public class FrontierGuide extends Card {

    public FrontierGuide() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}{G}",
                List.of(new SearchLibraryEffect(CardPredicateUtils.basicLand(), LibrarySearchDestination.BATTLEFIELD_TAPPED)),
                "{3}{G}, {T}: Search your library for a basic land card, put it onto the battlefield tapped, then shuffle."
        ));
    }
}
