package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "123")
public class CateranOverlord extends Card {

    public CateranOverlord() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeCreatureCost(), new RegenerateEffect()),
                "Sacrifice a creature: Regenerate this creature."));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{6}",
                List.of(new SearchLibraryEffect(
                        new CardAllOfPredicate(List.of(
                                new CardIsPermanentPredicate(),
                                new CardSubtypePredicate(CardSubtype.MERCENARY),
                                new CardMaxManaValuePredicate(6))),
                        LibrarySearchDestination.BATTLEFIELD)),
                "{6}, {T}: Search your library for a Mercenary permanent card with mana value 6 or less, put it onto the battlefield, then shuffle."));
    }
}
