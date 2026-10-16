package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "ONS", collectorNumber = "272")
@CardRegistration(set = "ARC", collectorNumber = "62")
@CardRegistration(set = "DDD", collectorNumber = "11")
@CardRegistration(set = "VMA", collectorNumber = "218")
@CardRegistration(set = "GVL", collectorNumber = "11")
@CardRegistration(set = "DDL", collectorNumber = "59")
@CardRegistration(set = "A25", collectorNumber = "178")
@CardRegistration(set = "DDU", collectorNumber = "14")
@CardRegistration(set = "MH1", collectorNumber = "169")
@CardRegistration(set = "HA3", collectorNumber = "21")
@CardRegistration(set = "C13", collectorNumber = "154")
@CardRegistration(set = "CMD", collectorNumber = "164")
@CardRegistration(set = "CMM", collectorNumber = "302")
public class KrosanTusker extends Card {

    public KrosanTusker() {
        addCycling("{2}{G}");
        addEffect(EffectSlot.ON_SELF_CYCLED,
                new MayEffect(new SearchLibraryEffect(
                        CardPredicateUtils.basicLand(), LibrarySearchDestination.HAND),
                        "Search your library for a basic land card?"));
    }
}
