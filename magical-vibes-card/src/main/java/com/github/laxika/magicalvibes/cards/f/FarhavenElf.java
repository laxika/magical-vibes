package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "SHM", collectorNumber = "113")
@CardRegistration(set = "SLD", collectorNumber = "766")
@CardRegistration(set = "C13", collectorNumber = "144")
@CardRegistration(set = "C14", collectorNumber = "196")
public class FarhavenElf extends Card {

    public FarhavenElf() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new SearchLibraryEffect(CardPredicateUtils.basicLand(), LibrarySearchDestination.BATTLEFIELD_TAPPED),
                        "Search your library for a basic land card?"));
    }
}
