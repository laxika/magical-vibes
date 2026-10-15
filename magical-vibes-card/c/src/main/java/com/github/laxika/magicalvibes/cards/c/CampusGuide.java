package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "FDN", collectorNumber = "251")
@CardRegistration(set = "STX", collectorNumber = "252")
public class CampusGuide extends Card {

    public CampusGuide() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SearchLibraryEffect(CardPredicateUtils.basicLand(), LibrarySearchDestination.TOP_OF_LIBRARY),
                "Search your library for a basic land card?"
        ));
    }
}
