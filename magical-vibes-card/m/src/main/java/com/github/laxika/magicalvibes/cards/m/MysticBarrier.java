package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AttackDirection;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AttackOnlyNearestOpponentInDirectionEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.SetAttackDirectionEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "C13", collectorNumber = "18")
public class MysticBarrier extends Card {

    public MysticBarrier() {
        ChooseOneEffect directionChoice = new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Left",
                        new SetAttackDirectionEffect(AttackDirection.LEFT)),
                new ChooseOneEffect.ChooseOneOption("Right",
                        new SetAttackDirectionEffect(AttackDirection.RIGHT))
        ));

        addTrigger(Triggers.enters(ObjectRelation.THIS), new ChooseOneAtResolutionEffect(directionChoice));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), directionChoice);
        addStaticEffect(new AttackOnlyNearestOpponentInDirectionEffect());
    }
}
