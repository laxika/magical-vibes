package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "KTK", collectorNumber = "230")
public class BloodstainedMire extends Card {

    public BloodstainedMire() {
        // {T}, Pay 1 life, Sacrifice this land: Search your library for a Swamp or Mountain card,
        // put it onto the battlefield, then shuffle.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new PayLifeCost(1),
                        new SacrificeSelfCost(),
                        new SearchLibraryEffect(
                                new CardAnyOfPredicate(List.of(
                                        new CardSubtypePredicate(CardSubtype.SWAMP),
                                        new CardSubtypePredicate(CardSubtype.MOUNTAIN))),
                                LibrarySearchDestination.BATTLEFIELD)
                ),
                "{T}, Pay 1 life, Sacrifice Bloodstained Mire: Search your library for a Swamp or Mountain card, put it onto the battlefield, then shuffle."
        ));
    }
}
