package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutChosenCardFromHandOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByDefendingPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "C17", collectorNumber = "44")
public class NazahnReveredBladesmith extends Card {

    public NazahnReveredBladesmith() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new SearchLibraryAndConditionalEffect(
                new CardSubtypePredicate(CardSubtype.EQUIPMENT),
                LibrarySearchDestination.HAND,
                new CardNamedPredicate("Hammer of Nazahn"),
                new PutChosenCardFromHandOntoBattlefieldEffect()));

        PermanentPredicate defendingCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledByDefendingPlayerPredicate()
        ));
        target(new PermanentPredicateTargetFilter(
                defendingCreature,
                "Target must be a creature defending player controls"
        )).addTrigger(Triggers.attacks(),
                new MayEffect(
                        new TapPermanentsEffect(TapUntapScope.TARGET, defendingCreature),
                        "Tap target creature defending player controls?"));
    }
}
