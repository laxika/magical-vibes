package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsSequenceCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentBlockingSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "150")
public class UrborgPanther extends Card {

    public UrborgPanther() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(new SacrificeSelfCost(), new DestroyTargetPermanentEffect()),
                "{B}, Sacrifice this creature: Destroy target creature blocking it.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentBlockingSourcePredicate()
                        )),
                        "Target must be a creature blocking this creature"
                )
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificePermanentsSequenceCost(
                                List.of(
                                        new PermanentNamedPredicate("Feral Shadow"),
                                        new PermanentNamedPredicate("Breathstealer"),
                                        new PermanentIsSourceCardPredicate()
                                ),
                                List.of("a creature named Feral Shadow", "a creature named Breathstealer", "this creature")
                        ),
                        new SearchLibraryEffect(
                                new CardNamedPredicate("Spirit of the Night"),
                                LibrarySearchDestination.BATTLEFIELD
                        )
                ),
                "Sacrifice a creature named Feral Shadow, a creature named Breathstealer, and this creature: "
                        + "Search your library for a card named Spirit of the Night, put that card onto the battlefield, then shuffle."
        ));
    }
}
