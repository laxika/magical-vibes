package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSharesCreatureTypeWithPermanentsPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "241")
@CardRegistration(set = "KHM", collectorNumber = "370")
public class PyreOfHeroes extends Card {

    public PyreOfHeroes() {
        // The sacrificed creature's mana value is X (a face-down one's is 0) and its creature types are read as it
        // last existed on the battlefield (the card's ruling; CR 608.2h).
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new SacrificeCreatureCost(true, false, false, false, null, true),
                        new SearchLibraryEffect(new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardSharesCreatureTypeWithPermanentsPredicate(Subjects.paidAsCost()))),
                                LibrarySearchDestination.BATTLEFIELD, new ManaValueBound(true, 1))),
                "{2}, {T}, Sacrifice a creature: Search your library for a creature card that shares a creature "
                        + "type with the sacrificed creature and has mana value equal to 1 plus that creature's "
                        + "mana value. Put that card onto the battlefield, then shuffle. Activate only as a sorcery.",
                null,
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
