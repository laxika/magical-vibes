package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSearchesLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "ODY", collectorNumber = "257")
public class NewFrontiers extends Card {

    public NewFrontiers() {
        addSpellEffect(EachPlayerSearchesLibraryEffect.maySearch(Subjects.eachPlayer(), CardPredicateUtils.basicLand(),
                new XValue(), LibrarySearchDestination.BATTLEFIELD_TAPPED));
    }
}
