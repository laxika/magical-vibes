package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "159")
public class DredgersInsight extends Card {

    public DredgersInsight() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                SequenceEffect.of(
                        new MillEffect(4, Subjects.you()),
                        ChooseCardEffect.upTo(
                                Subjects.cardsThisWay(StepKind.MILL, new CardAnyOfPredicate(List.of(
                                                new CardTypePredicate(CardType.ARTIFACT),
                                        new CardTypePredicate(CardType.CREATURE),
                                        new CardTypePredicate(CardType.LAND)))), 1,
                                "an artifact, creature, or land card milled this way"),
                        new PutCardsIntoHandEffect(Subjects.cardsThisWay(StepKind.CHOOSE))));
        addTrigger(new TriggerCondition(Triggers.leavesGraveyard(Triggers.ARTIFACT_OR_CREATURE,
                ObjectRelation.YOU_OWN)),
                TriggerOptions.ONE_OR_MORE,
                new GainLifeEffect(1));
    }
}
