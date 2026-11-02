package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZEN", collectorNumber = "18")
@CardRegistration(set = "DDI", collectorNumber = "13")
@CardRegistration(set = "KHC", collectorNumber = "28")
@CardRegistration(set = "ZNC", collectorNumber = "18")
public class KorCartographer extends Card {

    public KorCartographer() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                new SearchLibraryEffect(
                        new CardSubtypePredicate(CardSubtype.PLAINS),
                        LibrarySearchDestination.BATTLEFIELD_TAPPED),
                "Search your library for a Plains card?"));
    }
}
