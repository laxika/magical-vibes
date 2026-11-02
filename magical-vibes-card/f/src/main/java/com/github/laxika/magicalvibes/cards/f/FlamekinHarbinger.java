package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LRW", collectorNumber = "167")
@CardRegistration(set = "HOP", collectorNumber = "53")
@CardRegistration(set = "SLD", collectorNumber = "1255")
public class FlamekinHarbinger extends Card {

    public FlamekinHarbinger() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MayEffect(new SearchLibraryEffect(
                        new CardSubtypePredicate(CardSubtype.ELEMENTAL),
                        LibrarySearchDestination.TOP_OF_LIBRARY),
                        "Search your library for an Elemental card?"));
    }
}
