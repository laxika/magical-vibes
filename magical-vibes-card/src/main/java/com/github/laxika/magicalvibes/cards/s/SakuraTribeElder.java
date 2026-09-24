package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "239")
@CardRegistration(set = "SOC", collectorNumber = "285")
@CardRegistration(set = "SLD", collectorNumber = "192")
@CardRegistration(set = "C13", collectorNumber = "168")
@CardRegistration(set = "CMD", collectorNumber = "169")
@CardRegistration(set = "C15", collectorNumber = "200")
public class SakuraTribeElder extends Card {

    public SakuraTribeElder() {
        // Sacrifice this creature: Search your library for a basic land card, put that card onto
        // the battlefield tapped, then shuffle.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new SearchLibraryEffect(
                                CardPredicateUtils.basicLand(),
                                LibrarySearchDestination.BATTLEFIELD_TAPPED)
                ),
                "Sacrifice Sakura-Tribe Elder: Search your library for a basic land card, put that card onto the battlefield tapped, then shuffle."
        ));
    }
}
