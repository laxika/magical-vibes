package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.DuringDeclareAttackers;
import com.github.laxika.magicalvibes.model.condition.SourceAttacksActivatingPlayer;
import com.github.laxika.magicalvibes.model.effect.CantAttackControllerEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ReselectAttackingCreatureAttackTargetEffect;

import java.util.List;

@CardRegistration(set = "C20", collectorNumber = "58")
public class Capricopian extends Card {

    public Capricopian() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue()));
        addEffect(EffectSlot.STATIC, new CantAttackControllerEffect());
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        new MayEffect(
                                new ReselectAttackingCreatureAttackTargetEffect(true, true),
                                "Reselect which player this creature is attacking?")),
                "{2}: Put a +1/+1 counter on this creature, then you may reselect which player this creature is attacking."
        ).withActivatableByAnyPlayer().withActivationCondition(
                new AllConditions(List.of(new DuringDeclareAttackers(), new SourceAttacksActivatingPlayer())),
                "Only the player this creature is attacking may activate this ability and only during the declare attackers step."));
    }
}
