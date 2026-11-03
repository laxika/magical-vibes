package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsMoreLands;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "40K", collectorNumber = "15")
public class SpaceMarineScout extends Card {

    public SpaceMarineScout() {
        // Concealed Position — When this creature enters, if an opponent controls more lands than
        // you, you may search your library for a Plains card, put it onto the battlefield tapped,
        // then shuffle.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                TriggerOptions.DEFAULT.withInterveningIf(new OpponentControlsMoreLands()),
                new MayEffect(
                        new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.PLAINS),
                                LibrarySearchDestination.BATTLEFIELD_TAPPED),
                        "Search your library for a Plains card?"));
    }
}
