package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
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
        CardPredicate hammer = new CardNamedPredicate("Hammer of Nazahn");
        addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.EQUIPMENT),
                        LibrarySearchDestination.REVEAL_ONLY, null, false),
                new PutCardsOntoBattlefieldEffect(Subjects.cardsThisWay(StepKind.SEARCH, hammer)),
                new PutCardsIntoHandEffect(Subjects.cardsThisWay(StepKind.SEARCH, new CardNotPredicate(hammer))),
                new ShuffleLibraryEffect(false)));

        PermanentPredicate defendingCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledByDefendingPlayerPredicate()
        ));
        target(new PermanentPredicateTargetFilter(
                defendingCreature,
                "Target must be a creature defending player controls"
        )).addTrigger(Triggers.attacks(),
                new MayEffect(
                        new TapPermanentsEffect(Subjects.target(), defendingCreature),
                        "Tap target creature defending player controls?"));
    }
}
