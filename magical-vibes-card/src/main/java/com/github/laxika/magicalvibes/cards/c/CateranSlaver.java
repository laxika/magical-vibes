package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "125")
public class CateranSlaver extends Card {

    public CateranSlaver() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}",
                List.of(new SearchLibraryEffect(
                        new CardAllOfPredicate(List.of(
                                new CardIsPermanentPredicate(),
                                new CardSubtypePredicate(CardSubtype.MERCENARY),
                                new CardMaxManaValuePredicate(5))),
                        LibrarySearchDestination.BATTLEFIELD)),
                "{5}, {T}: Search your library for a Mercenary permanent card with mana value 5 or less, put it onto the battlefield, then shuffle."));
    }
}
