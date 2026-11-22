package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ACR", collectorNumber = "13")
public class BalladOfTheBlackFlag extends Card {

    public BalladOfTheBlackFlag() {
        CardIsHistoricPredicate historic = new CardIsHistoricPredicate();

        addTrigger(Triggers.chapter(1),
                SequenceEffect.of(
                        new MillEffect(3, Subjects.you()),
                        ChooseCardEffect.upTo(Subjects.cardsThisWay(StepKind.MILL, historic), 1,
                                "a historic card milled this way"),
                        new PutCardsIntoHandEffect(Subjects.cardsThisWay(StepKind.CHOOSE))));
        addTrigger(Triggers.chapter(2),
                SequenceEffect.of(
                        new MillEffect(3, Subjects.you()),
                        ChooseCardEffect.upTo(Subjects.cardsThisWay(StepKind.MILL, historic), 1,
                                "a historic card milled this way"),
                        new PutCardsIntoHandEffect(Subjects.cardsThisWay(StepKind.CHOOSE))));
        addTrigger(Triggers.chapter(3),
                SequenceEffect.of(
                        new MillEffect(3, Subjects.you()),
                        ChooseCardEffect.upTo(Subjects.cardsThisWay(StepKind.MILL, historic), 1,
                                "a historic card milled this way"),
                        new PutCardsIntoHandEffect(Subjects.cardsThisWay(StepKind.CHOOSE))));
        addTrigger(Triggers.chapter(4),
                new ReduceCastCostForMatchingSpellsUntilEndOfTurnEffect(
                        historic, 2));
    }
}
