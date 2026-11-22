package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSearchesLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "SLD", collectorNumber = "1009")
@CardRegistration(set = "CMD", collectorNumber = "147")
@CardRegistration(set = "C16", collectorNumber = "145")
public class CollectiveVoyage extends Card {

    public CollectiveVoyage() {
        addSpellEffect(SequenceEffect.of(
                EachPlayerPaysAnyManaEffect.joinForces(),
                EachPlayerSearchesLibraryEffect.mustSearch(Subjects.eachPlayer(), CardPredicateUtils.basicLand(),
                        AmountThisWay.manaPaid(), LibrarySearchDestination.BATTLEFIELD_TAPPED)));
    }
}
