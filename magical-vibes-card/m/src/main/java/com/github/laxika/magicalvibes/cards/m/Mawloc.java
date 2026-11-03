package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.SpellXAtLeast;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MarkTargetCreatureExileInsteadOfDieThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "40K", collectorNumber = "133")
public class Mawloc extends Card {

    public Mawloc() {
        // Ravenous — this creature enters with X +1/+1 counters on it. If X is 5 or more,
        // draw a card when it enters.
        addStaticEffect(new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue()));
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                TriggerMode.INDEPENDENT,
                TriggerOptions.DEFAULT.withInterveningIf(new SpellXAtLeast(5)),
                new DrawCardEffect(1));

        // Terror from the Deep — when this creature enters, it fights up to one target creature
        // an opponent controls. If that creature would die this turn, exile it instead.
        target(TargetFilters.creatureAnOpponentControls(), 0, 1)
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new MarkTargetCreatureExileInsteadOfDieThisTurnEffect())
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new EnteringCreatureFightsTargetCreatureEffect());
    }
}
