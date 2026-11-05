package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.ControllerEnergyAtLeast;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreaturePermanentlyEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "M3C", collectorNumber = "45")
@CardRegistration(set = "M3C", collectorNumber = "97")
public class AuroraShifter extends Card {

    public AuroraShifter() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new EnergyCountersEffect(new EventValue()));

        PermanentAllOfPredicate anotherCreatureYouControl = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));

        target(new PermanentPredicateTargetFilter(
                anotherCreatureYouControl,
                "Target must be another creature you control"
        )).addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU), new MayEffect(
                new ConditionalEffect(new ControllerEnergyAtLeast(2),
                        SequenceEffect.of(
                                new EnergyCountersEffect(-2),
                                new QueueReflexiveAbilityEffect(new BecomeCopyOfTargetCreaturePermanentlyEffect(
                                        null,
                                        Triggers.beginningOfCombat(ActivePlayer.YOU),
                                        anotherCreatureYouControl,
                                        List.of(Triggers.dealsCombatDamageToPlayer()))))),
                "Pay {E}{E} to have Aurora Shifter become a copy of another creature you control?"));
    }
}
