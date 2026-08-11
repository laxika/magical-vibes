package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "213")
public class BurnishedHart extends Card {

    public BurnishedHart() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(
                        new SacrificeSelfCost(),
                        new SearchLibraryEffect(
                                new Fixed(2), CardPredicateUtils.basicLand(), LibrarySearchDestination.BATTLEFIELD_TAPPED)
                ),
                "{3}, Sacrifice this creature: Search your library for up to two basic land cards, "
                        + "put them onto the battlefield tapped, then shuffle."
        ));
    }
}
