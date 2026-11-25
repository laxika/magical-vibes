package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "137")
public class DragonsparkReactor extends Card {

    public DragonsparkReactor() {
        addTriggeredAbility(new TriggeredAbility(List.of(
                        new TriggerCondition(Triggers.enters(ObjectRelation.THIS)),
                        new TriggerCondition(Triggers.enters(Triggers.ARTIFACT, ObjectRelation.ANOTHER_YOU_CONTROL))),
                TriggerOptions.DEFAULT, List.of(new EffectRegistration(
                        PutCountersEffect.onSelf(CounterType.CHARGE)))));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}",
                List.of(
                        new SacrificeSelfCost(),
                        SequenceEffect.simultaneous(
                                DealDamageEffect.toAnyTargetInGroup(new CountersOnSource(CounterType.CHARGE), 0),
                                DealDamageEffect.toAnyTargetInGroup(new CountersOnSource(CounterType.CHARGE), 1))
                ),
                "{4}, Sacrifice this artifact: It deals damage equal to the number of charge counters on it "
                        + "to target player and that much damage to up to one target creature.",
                List.of(
                        new PlayerPredicateTargetFilter(
                                new PlayerRelationPredicate(PlayerRelation.ANY),
                                "Target must be a player"),
                        new PermanentPredicateTargetFilter(
                                new PermanentIsCreaturePredicate(),
                                "Target must be a creature")
                ),
                1,
                2
        ));
    }
}
