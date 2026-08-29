package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "EMN", collectorNumber = "167")
public class PrimalDruid extends Card {

    public PrimalDruid() {
        addEffect(EffectSlot.ON_DEATH, new MayEffect(
                new SearchLibraryEffect(
                        CardPredicateUtils.basicLand(),
                        LibrarySearchDestination.BATTLEFIELD_TAPPED),
                "Search your library for a basic land card, put it onto the battlefield tapped?"));
    }
}
