package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.CardsInHandGraveyardAndLibraryMatchingAtLeast;
import com.github.laxika.magicalvibes.model.condition.WasCast;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YMID", collectorNumber = "8")
public class InquisitorCaptain extends Card {

    public InquisitorCaptain() {
        CardAllOfPredicate eligibleCreature = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardMaxManaValuePredicate(3)));
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new AllOf(List.of(
                                new WasCast(),
                                new CardsInHandGraveyardAndLibraryMatchingAtLeast(20, eligibleCreature)))),
                SequenceEffect.of(
                        new SeekLibraryEffect(2, eligibleCreature),
                        ChooseCardEffect.oneOf(Subjects.cardsThisWay(StepKind.SEEK),
                                "a sought creature card to put onto the battlefield"),
                        new PutCardsOntoBattlefieldEffect(Subjects.cardsThisWay(StepKind.CHOOSE)),
                        PutCardsIntoLibraryEffect.onBottomInRandomOrder(Subjects.except(
                                Subjects.cardsThisWay(StepKind.SEEK), Subjects.cardsThisWay(StepKind.CHOOSE))),
                        new ShuffleLibraryEffect(false)));
    }
}
