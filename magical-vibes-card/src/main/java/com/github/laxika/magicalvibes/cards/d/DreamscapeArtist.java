package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "40")
public class DreamscapeArtist extends Card {

    public DreamscapeArtist() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{U}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new SacrificePermanentCost(new PermanentIsLandPredicate(), "a land"),
                        new SearchLibraryEffect(
                                new Fixed(2),
                                CardPredicateUtils.basicLand(),
                                LibrarySearchDestination.BATTLEFIELD)
                ),
                "{2}{U}, {T}, Discard a card, Sacrifice a land: Search your library for up to two basic land cards, "
                        + "put them onto the battlefield, then shuffle."
        ));
    }
}
