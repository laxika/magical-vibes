package com.github.laxika.magicalvibes.cards.r;

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

@CardRegistration(set = "MMQ", collectorNumber = "35")
public class RamosianCaptain extends Card {

    public RamosianCaptain() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}",
                List.of(new SearchLibraryEffect(
                        new CardAllOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.REBEL),
                                new CardIsPermanentPredicate(),
                                new CardMaxManaValuePredicate(4))),
                        LibrarySearchDestination.BATTLEFIELD)),
                "{5}, {T}: Search your library for a Rebel permanent card with mana value 4 or less, "
                        + "put it onto the battlefield, then shuffle."
        ));
    }
}
