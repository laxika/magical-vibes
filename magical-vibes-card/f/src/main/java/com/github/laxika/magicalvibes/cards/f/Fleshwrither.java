package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.SourceManaValueMinusOne;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "FUT", collectorNumber = "84")
public class Fleshwrither extends Card {

    public Fleshwrither() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}{B}",
                List.of(
                        new SacrificeSelfCost(),
                        new SearchLibraryEffect(
                                new CardTypePredicate(CardType.CREATURE),
                                LibrarySearchDestination.BATTLEFIELD,
                                new ManaValueBound(new SourceManaValueMinusOne(), true, 1))
                ),
                "Transfigure {1}{B}{B} ({1}{B}{B}, Sacrifice this creature: Search your library for a creature card "
                        + "with the same mana value as this creature, put that card onto the battlefield, then shuffle. "
                        + "Transfigure only as a sorcery.)",
                null,
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
