package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LGN", collectorNumber = "103")
@CardRegistration(set = "MKC", collectorNumber = "155")
@CardRegistration(set = "ARC", collectorNumber = "42")
public class ImperialHellkite extends Card {

    public ImperialHellkite() {
        addMorph("{6}{R}{R}");
        addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS), new MayEffect(
                new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.DRAGON)),
                "Search your library for a Dragon card?"
        ));
    }
}
