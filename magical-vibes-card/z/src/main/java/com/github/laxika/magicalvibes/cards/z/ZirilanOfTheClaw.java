package com.github.laxika.magicalvibes.cards.z;

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

@CardRegistration(set = "MIR", collectorNumber = "204")
public class ZirilanOfTheClaw extends Card {

    public ZirilanOfTheClaw() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{R}{R}",
                List.of(new SearchLibraryEffect(
                        new CardAllOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.DRAGON),
                                new CardIsPermanentPredicate())),
                        LibrarySearchDestination.BATTLEFIELD,
                        true,
                        true)),
                "{1}{R}{R}, {T}: Search your library for a Dragon permanent card, put that card onto the battlefield, "
                        + "then shuffle. That Dragon gains haste until end of turn. "
                        + "Exile it at the beginning of the next end step."
        ));
    }
}
