package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSearchesLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "TMC", collectorNumber = "129")
public class TurtleTracks extends Card {

    public TurtleTracks() {
        target(0, 99).addSpellEffect(EachPlayerSearchesLibraryEffect.maySearch(
                Subjects.targetPlayer(), CardPredicateUtils.basicLand(), LibrarySearchDestination.BATTLEFIELD));
    }
}
