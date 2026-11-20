package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CollectEvidenceEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPermanentsTargetControlsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "132")
@CardRegistration(set = "MKM", collectorNumber = "349")
@CardRegistration(set = "MKM", collectorNumber = "381")
public class IncineratorOfTheGuilty extends Card {

    public IncineratorOfTheGuilty() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new MayEffect(
                SequenceEffect.of(
                        CollectEvidenceEffect.withChosenX(),
                        new QueueReflexiveAbilityEffect(new DealDamageToPermanentsTargetControlsEffect(
                                new EventValue(), Subjects.controlledBy(Subjects.triggeringPlayer(),
                                        new PermanentAnyOfPredicate(List.of(new PermanentIsCreaturePredicate(),
                                                new PermanentIsPlaneswalkerPredicate())))))),
                "Collect evidence X?"));
    }
}
