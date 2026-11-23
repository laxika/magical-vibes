package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.amount.CountersOnTriggeringPermanent;
import com.github.laxika.magicalvibes.model.amount.Divided;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.NoteAmountEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentCounterCountAtLeastPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "196")
public class OchreJelly extends Card {

    public OchreJelly() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue()));
        addTrigger(Triggers.dies(new PermanentCounterCountAtLeastPredicate(CounterType.PLUS_ONE_PLUS_ONE, 2),
                        ObjectRelation.THIS),
                SequenceEffect.of(
                        new NoteAmountEffect(new Divided(
                                new CountersOnTriggeringPermanent(CounterType.PLUS_ONE_PLUS_ONE), 2)),
                        RegisterDelayedStepTriggerEffect.atNext(Triggers.endStep(ActivePlayer.ANY),
                                new CreateTokenCopyOfSourceEffect(false, 1, AmountThisWay.noted()))));
    }
}
