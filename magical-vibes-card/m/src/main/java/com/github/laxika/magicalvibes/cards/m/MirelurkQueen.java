package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GiveRadCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PIP", collectorNumber = "33")
@CardRegistration(set = "PIP", collectorNumber = "561")
@CardRegistration(set = "PIP", collectorNumber = "377")
@CardRegistration(set = "PIP", collectorNumber = "905")
public class MirelurkQueen extends Card {

    public MirelurkQueen() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new GiveRadCountersEffect(2, Subjects.targetPlayer()));

        addTrigger(new TriggerCondition(Triggers.mills(Triggers.NONLAND, ObjectRelation.ANY)),
                TriggerOptions.ONE_OR_MORE.withLimit(TriggerLimit.ONCE_EACH_TURN),
                SequenceEffect.of(new DrawCardEffect(1), PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE)));
    }
}
