package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "BFZ", collectorNumber = "180")
@CardRegistration(set = "C21", collectorNumber = "201")
@CardRegistration(set = "ZNC", collectorNumber = "76")
public class NissasRenewal extends Card {

    public NissasRenewal() {
        addSpellEffect(new SearchLibraryEffect(new Fixed(3), CardPredicateUtils.basicLand(),
                LibrarySearchDestination.BATTLEFIELD_TAPPED));
        addSpellEffect(new GainLifeEffect(7));
    }
}
