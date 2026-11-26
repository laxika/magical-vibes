package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ExileOtherNonlandPermanentsWithManaValueAtMostEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KHM", collectorNumber = "228")
@CardRegistration(set = "KHM", collectorNumber = "330")
@CardRegistration(set = "SLD", collectorNumber = "2240")
public class SarulfRealmEater extends Card {

    public SarulfRealmEater() {
        addTrigger(Triggers.dies(null, ObjectRelation.OPPONENT_CONTROLS),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));

        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceCounterThreshold(1, CounterType.PLUS_ONE_PLUS_ONE)),
                new MayEffect(
                        SequenceEffect.of(
                                RemoveCountersEffect.allFromSelf(CounterType.PLUS_ONE_PLUS_ONE),
                                new ExileOtherNonlandPermanentsWithManaValueAtMostEffect(
                                        new AmountThisWay(StepKind.REMOVE_COUNTERS))),
                        "Remove all +1/+1 counters from Sarulf, Realm Eater?"));
    }
}
