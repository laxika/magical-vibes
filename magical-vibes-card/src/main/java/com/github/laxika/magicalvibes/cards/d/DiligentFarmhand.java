package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "237")
public class DiligentFarmhand extends Card {

    public DiligentFarmhand() {
        // {1}{G}, Sacrifice this creature: Search your library for a basic land card, put that
        // card onto the battlefield tapped, then shuffle.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(
                        new SacrificeSelfCost(),
                        new SearchLibraryEffect(
                                CardPredicateUtils.basicLand(),
                                LibrarySearchDestination.BATTLEFIELD_TAPPED)
                ),
                "{1}{G}, Sacrifice Diligent Farmhand: Search your library for a basic land card, put that card onto the battlefield tapped, then shuffle."
        ));
    }
}
