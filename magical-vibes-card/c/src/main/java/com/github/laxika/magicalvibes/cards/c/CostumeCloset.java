package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MoveCounterFromSourceToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsModifiedPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "5")
@CardRegistration(set = "OM1", collectorNumber = "7")
@CardRegistration(set = "MSC", collectorNumber = "770")
public class CostumeCloset extends Card {

    public CostumeCloset() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(2)));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new MoveCounterFromSourceToTargetCreatureEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                "{T}: Move a +1/+1 counter from this artifact onto target creature you control. Activate only as a sorcery.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(), "Target must be a creature you control"),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));

        // "A modified creature you control", this artifact included once it is one.
        var modifiedCreature = new PermanentAllOfPredicate(List.of(Triggers.CREATURE,
                new PermanentIsModifiedPredicate()));
        addTriggeredAbility(new TriggeredAbility(List.of(
                new TriggerCondition(Triggers.leavesBattlefield(modifiedCreature, ObjectRelation.THIS)),
                new TriggerCondition(Triggers.leavesBattlefield(modifiedCreature, ObjectRelation.ANOTHER_YOU_CONTROL))),
                TriggerOptions.DEFAULT, List.of(new EffectRegistration(new PutCountersOnSourceEffect(1, 1, 1)))));
    }
}
