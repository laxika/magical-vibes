package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSearchesLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "260")
@CardRegistration(set = "M3C", collectorNumber = "335")
@CardRegistration(set = "OTC", collectorNumber = "283")
@CardRegistration(set = "FIC", collectorNumber = "385")
public class DemolitionField extends Card {

    public DemolitionField() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {2}, {T}, Sacrifice Demolition Field: Destroy target nonbasic land an opponent controls.
        // That land's controller may search their library for a basic land card, put it onto the
        // battlefield, then shuffle. You may search your library for a basic land card, put it
        // onto the battlefield, then shuffle.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new SacrificeSelfCost(),
                        SequenceEffect.of(
                                new DestroyTargetPermanentEffect(),
                                // Destroyed or not, its controller searches first, then you (its ruling; CR 608.2c).
                                EachPlayerSearchesLibraryEffect.maySearch(Subjects.controllerOf(Subjects.target()),
                                        CardPredicateUtils.basicLand(), LibrarySearchDestination.BATTLEFIELD),
                                EachPlayerSearchesLibraryEffect.maySearch(Subjects.you(),
                                        CardPredicateUtils.basicLand(), LibrarySearchDestination.BATTLEFIELD))
                ),
                "{2}, {T}, Sacrifice Demolition Field: Destroy target nonbasic land an opponent controls. That land's controller may search their library for a basic land card, put it onto the battlefield, then shuffle. You may search your library for a basic land card, put it onto the battlefield, then shuffle.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsLandPredicate(),
                                new PermanentNotPredicate(new PermanentHasSupertypePredicate(CardSupertype.BASIC)),
                                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
                        )),
                        "Target must be a nonbasic land an opponent controls"
                )
        ));
    }
}
