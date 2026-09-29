package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsMoreLands;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "40K", collectorNumber = "15")
public class SpaceMarineScout extends Card {

    public SpaceMarineScout() {
        // Concealed Position — When this creature enters, if an opponent controls more lands than
        // you, you may search your library for a Plains card, put it onto the battlefield tapped,
        // then shuffle.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(new OpponentControlsMoreLands(),
                        new MayEffect(
                                new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.PLAINS),
                                        LibrarySearchDestination.BATTLEFIELD_TAPPED),
                                "Search your library for a Plains card?")));
    }
}
