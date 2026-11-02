package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TSP", collectorNumber = "235")
@CardRegistration(set = "TSR", collectorNumber = "247")
@CardRegistration(set = "C20", collectorNumber = "197")
@CardRegistration(set = "ARC", collectorNumber = "75")
public class YavimayaDryad extends Card {

    public YavimayaDryad() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.FOREST),
                        LibrarySearchDestination.BATTLEFIELD_TAPPED_UNDER_TARGET_PLAYER),
                "Search your library for a Forest card?"));
    }
}
