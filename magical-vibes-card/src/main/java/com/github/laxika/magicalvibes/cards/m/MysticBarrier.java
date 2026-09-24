package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AttackDirection;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AttackOnlyNearestOpponentInDirectionEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.SetAttackDirectionEffect;

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

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneAtTriggerTimeEffect(directionChoice));
        addEffect(EffectSlot.UPKEEP_TRIGGERED, directionChoice);
        addEffect(EffectSlot.STATIC, new AttackOnlyNearestOpponentInDirectionEffect());
    }
}
