package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

import java.util.List;

@CardRegistration(set = "AER", collectorNumber = "171")
public class PlanarBridge extends Card {

    public PlanarBridge() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{8}",
                List.of(new SearchLibraryEffect(
                        new CardIsPermanentPredicate(), LibrarySearchDestination.BATTLEFIELD)),
                "{8}, {T}: Search your library for a permanent card, put it onto the battlefield, then shuffle."
        ));
    }
}
