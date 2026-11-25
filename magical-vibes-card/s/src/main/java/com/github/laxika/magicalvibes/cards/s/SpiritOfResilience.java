package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfSubjectEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SOC", collectorNumber = "37")
@CardRegistration(set = "SOC", collectorNumber = "85")
public class SpiritOfResilience extends Card {

    public SpiritOfResilience() {
        addTrigger(new TriggerCondition(Triggers.leavesGraveyard(null, ObjectRelation.YOU_OWN)),
                TriggerOptions.ONE_OR_MORE,
                SequenceEffect.of(
                        PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE),
                        ChooseCardEffect.upTo(Subjects.triggeringCardsAsTheyLastExisted(new CardAnyOfPredicate(
                                        List.of(new CardTypePredicate(CardType.ARTIFACT),
                                                new CardTypePredicate(CardType.CREATURE)))), 1,
                                "an artifact or creature card from among those cards"),
                        BecomeCopyOfSubjectEffect.of(Subjects.cardsThisWay(StepKind.CHOOSE)).untilEndOfTurn()));
    }
}
