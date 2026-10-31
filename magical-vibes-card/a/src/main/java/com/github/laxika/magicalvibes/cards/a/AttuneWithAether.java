package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "KLD", collectorNumber = "145")
@CardRegistration(set = "KLR", collectorNumber = "154")
@CardRegistration(set = "DRC", collectorNumber = "109")
public class AttuneWithAether extends Card {

    public AttuneWithAether() {
        addSpellEffect(new SearchLibraryEffect(CardPredicateUtils.basicLand(), LibrarySearchDestination.HAND));
        addSpellEffect(new EnergyCountersEffect(2));
    }
}
