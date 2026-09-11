package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "10E", collectorNumber = "360")
@CardRegistration(set = "M10", collectorNumber = "229")
@CardRegistration(set = "M11", collectorNumber = "229")
@CardRegistration(set = "ONE", collectorNumber = "261")
@CardRegistration(set = "DSK", collectorNumber = "269")
@CardRegistration(set = "SOS", collectorNumber = "265")
@CardRegistration(set = "TSP", collectorNumber = "279")
@CardRegistration(set = "DDE", collectorNumber = "66")
@CardRegistration(set = "H09", collectorNumber = "34")
@CardRegistration(set = "DDH", collectorNumber = "76")
@CardRegistration(set = "HOP", collectorNumber = "139")
@CardRegistration(set = "PC2", collectorNumber = "129")
public class TerramorphicExpanse extends Card {

    public TerramorphicExpanse() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new SearchLibraryEffect(
                                new CardAllOfPredicate(List.of(new CardSupertypePredicate(CardSupertype.BASIC), new CardTypePredicate(CardType.LAND))),
                                LibrarySearchDestination.BATTLEFIELD_TAPPED)
                ),
                "{T}, Sacrifice Terramorphic Expanse: Search your library for a basic land card, put it onto the battlefield tapped, then shuffle."
        ));
    }
}
