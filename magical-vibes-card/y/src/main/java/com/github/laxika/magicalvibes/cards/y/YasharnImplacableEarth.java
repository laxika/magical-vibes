package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.PlayersCantPayLifeOrSacrificeNonlandPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForOneCardOfEachSubtypeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "240")
public class YasharnImplacableEarth extends Card {

    public YasharnImplacableEarth() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new SearchLibraryForOneCardOfEachSubtypeEffect(
                List.of(CardSubtype.FOREST, CardSubtype.PLAINS), null, LibrarySearchDestination.HAND, true));
        addStaticEffect(new PlayersCantPayLifeOrSacrificeNonlandPermanentsEffect());
    }
}
