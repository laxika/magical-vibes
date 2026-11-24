package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "24")
@CardRegistration(set = "MMA", collectorNumber = "31")
@CardRegistration(set = "2XM", collectorNumber = "32")
@CardRegistration(set = "LTC", collectorNumber = "521")
public class StonehewerGiant extends Card {

    public StonehewerGiant() {
        // {1}{W}, {T}: Search your library for an Equipment card, put it onto the battlefield,
        // attach it to a creature you control, then shuffle.
        addActivatedAbility(new ActivatedAbility(
                true,       // requires tap
                "{1}{W}",
                List.of(new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.EQUIPMENT),
                        LibrarySearchDestination.BATTLEFIELD_ATTACHED_TO_CREATURE)),
                "{1}{W}, {T}: Search your library for an Equipment card, put it onto the battlefield, attach it to a creature you control, then shuffle."
        ));
    }
}
