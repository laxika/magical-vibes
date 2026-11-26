package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.amount.CountersOnTriggeringPermanent;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.WasCast;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.NoteAmountEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;

@CardRegistration(set = "FDN", collectorNumber = "66")
@CardRegistration(set = "FDN", collectorNumber = "321")
@CardRegistration(set = "FDN", collectorNumber = "385")
@CardRegistration(set = "FDN", collectorNumber = "462")
public class NineLivesFamiliar extends Card {

    public NineLivesFamiliar() {
        addStaticEffect(new ConditionalEffect(new WasCast(),
                new EnterWithCountersEffect(CounterType.REVIVAL, new Fixed(8))));
        addTrigger(Triggers.dies(new PermanentHasCountersPredicate(CounterType.REVIVAL), ObjectRelation.THIS),
                SequenceEffect.of(
                        new NoteAmountEffect(new Sum(new CountersOnTriggeringPermanent(CounterType.REVIVAL),
                                new Fixed(-1))),
                        RegisterDelayedStepTriggerEffect.aboutCardAtNext(Triggers.endStep(ActivePlayer.ANY),
                                Subjects.sourceCard(), new PutCardsOntoBattlefieldEffect(Subjects.triggeringCards())
                                .underControlOf(Subjects.you()).withCounters(CounterType.REVIVAL, AmountThisWay.noted()))));
    }
}
