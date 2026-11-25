package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ODY", collectorNumber = "166")
public class TravelingPlague extends Card {

    public TravelingPlague() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.upkeep(ActivePlayer.ANY), PutCountersEffect.onSelf(CounterType.PLAGUE))
                .addStaticEffect(new AttachedBoostEffect(
                        new Scaled(new CountersOnSource(CounterType.PLAGUE), -1),
                        new Scaled(new CountersOnSource(CounterType.PLAGUE), -1),
                        Subjects.attachedCreature()))
                .addTrigger(Triggers.leavesBattlefield(null, ObjectRelation.ENCHANTED),
                        new PutCardsOntoBattlefieldEffect(Subjects.sourceCard())
                                .underControlOf(Subjects.controllerOf(Subjects.triggering())));
    }
}
