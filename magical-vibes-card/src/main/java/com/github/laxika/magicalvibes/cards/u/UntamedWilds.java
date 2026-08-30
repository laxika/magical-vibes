package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "POR", collectorNumber = "191")
@CardRegistration(set = "P02", collectorNumber = "149")
@CardRegistration(set = "7ED", collectorNumber = "279")
@CardRegistration(set = "6ED", collectorNumber = "263")
@CardRegistration(set = "5ED", collectorNumber = "335")
@CardRegistration(set = "4ED", collectorNumber = "279")
@CardRegistration(set = "ITP", collectorNumber = "44")
@CardRegistration(set = "RQS", collectorNumber = "43")
@CardRegistration(set = "S99", collectorNumber = "148")
@CardRegistration(set = "LEG", collectorNumber = "210")
public class UntamedWilds extends Card {

    public UntamedWilds() {
        // Search your library for a basic land card, put that card onto the battlefield, then shuffle.
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(
                CardPredicateUtils.basicLand(), LibrarySearchDestination.BATTLEFIELD));
    }
}
