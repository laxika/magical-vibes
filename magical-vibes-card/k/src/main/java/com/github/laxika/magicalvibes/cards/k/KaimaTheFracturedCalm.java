package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GoadTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantedBySourceControllerAuraPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "NEC", collectorNumber = "3")
@CardRegistration(set = "NEC", collectorNumber = "74")
public class KaimaTheFracturedCalm extends Card {

    public KaimaTheFracturedCalm() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                SequenceEffect.of(
                        GoadTargetCreatureUntilNextTurnEffect.of(Subjects.controlledBy(Subjects.eachOpponent(),
                                        new PermanentAllOfPredicate(List.of(
                                                new PermanentIsCreaturePredicate(),
                                                new PermanentIsEnchantedBySourceControllerAuraPredicate()))),
                                Duration.UNTIL_YOUR_NEXT_TURN),
                        new ConditionalEffect(new HappenedThisWay(StepKind.GOAD), PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE, new AmountThisWay(StepKind.GOAD)))));
    }
}
