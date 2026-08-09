package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "NEM", collectorNumber = "41")
public class Seahunter extends Card {

    public Seahunter() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new SearchLibraryEffect(
                        new CardAllOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.MERFOLK),
                                new CardIsPermanentPredicate())),
                        LibrarySearchDestination.BATTLEFIELD)),
                "{3}, {T}: Search your library for a Merfolk permanent card, put it onto the battlefield, then shuffle."
        ));
    }
}
